package com.green_computer.green_board.service;

import com.green_computer.green_board.dto.*;
import com.green_computer.green_board.entity.AccessTokenBlacklist;
import com.green_computer.green_board.entity.RefreshToken;
import com.green_computer.green_board.entity.User;
import com.green_computer.green_board.enums.UserRole;
import com.green_computer.green_board.enums.UserStatus;
import com.green_computer.green_board.exceptions.AuthenticationFailureException;
import com.green_computer.green_board.exceptions.ResourceNotFoundException;
import com.green_computer.green_board.global.TokenProvider;
import com.green_computer.green_board.repository.AccessTokenBlacklistRepository;
import com.green_computer.green_board.repository.RefreshTokenRepository;
import com.green_computer.green_board.repository.UserRepository;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

@Service
@Slf4j
@AllArgsConstructor
public class AuthService {
    private final AccessTokenBlacklistRepository accessTokenBlacklistRepository;
    private UserRepository userRepository;
    private RefreshTokenRepository refreshTokenRepository;
    private PasswordEncoder passwordEncoder;
    private TokenProvider tokenProvider;

    private static final long REFRESH_TOKEN_VALIDITY = 1000 * 60 * 60 * 24;

    public LoginResponse login(UserLoginRequest userLoginRequest, HttpSession session) {
        // 엑세스토큰, 리프레시 토큰
        User user = userRepository.findByUsername(userLoginRequest.getUsername());
        if(user == null){
            throw new ResourceNotFoundException("User not found");
        }

        if(!passwordEncoder.matches(userLoginRequest.getPassword(), user.getPassword())){
            // 실패했으면 401
            throw new AuthenticationFailureException("Wrong password");
        }

        // 로그인 성공
        String accessToken = tokenProvider.generateAccessToken(user.getUsername());
        String refreshToken = tokenProvider.generateRefreshToken(user.getUsername());

        // Refresh Token 을 DB에 추가
        RefreshToken refresh = new RefreshToken();
        refresh.setToken(refreshToken);
        refresh.setUser(user);
        refresh.setExpirationDateTime(LocalDateTime.now().plusSeconds(REFRESH_TOKEN_VALIDITY/1000));

        refreshTokenRepository.save(refresh);

        return new LoginResponse(accessToken, refreshToken);
    }

    public void register(UserRegisterRequest userRegisterRequest) {
        String encodedPassword = passwordEncoder.encode(userRegisterRequest.getPassword());

        User user = new User();
        user.setUsername(userRegisterRequest.getUsername());
        user.setPassword(encodedPassword);
        user.setName(userRegisterRequest.getName());
        user.setRole(UserRole.USER);
        user.setStatus(UserStatus.ACTIVE);

        userRepository.save(user);
    }

    @Transactional
    public void logout(LogoutRequest logoutRequest) {
        // 행동 : 로그아웃 할 사용자의 리프레시 토큰을 DB에서 지운다.

        // 1. 지금 로그아웃을 요청한 사용자의 유저 네임을 알아낸다
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        // 2. 사용자의 유저네임을 통해 사용자의 id (pk) 를 알아낸다
        int userId = userRepository.findByUsername(username).getId();
        // 3. refresh_token 테이블에서 해당 사용자의 모든 refresh token을 찾아 지운다.
        refreshTokenRepository.deleteByUserId(userId);

        // 행동 : 로그아웃 할 사용자의 엑세스토큰을 블랙리스트에 삽입한다.

        // 1. 지금 로그아웃을 요청한 사용자의 엑세스 토큰을 가져온다
        String accessToken = logoutRequest.getAccessToken();
        // 2. 해당 엑세스 토큰을 블랙리스트 DB에 삽입한다.
        AccessTokenBlacklist accessTokenBlacklist = new AccessTokenBlacklist();
        accessTokenBlacklist.setToken(accessToken);
        // 2-1. 만료 일자를 토큰으로부터 뽑아온다.
        Date expDate = tokenProvider.getExpiration(accessToken);
        // 2-2. 해당 만료일자를 DB에 넣기 위해 Date -> LocalDateTime 변환을 시행한다.
        LocalDateTime convertedDateTime = expDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        // 2-3. 세팅한다.
        accessTokenBlacklist.setExpirationDateTime(convertedDateTime);


        accessTokenBlacklistRepository.save(accessTokenBlacklist);

    }

    public RefreshResponse refresh(RefreshRequest refreshRequest) {
        String userRefreshToken = refreshRequest.getRefreshToken();
        // 1. 위/변조 여부 검증
        if(!tokenProvider.validateToken(userRefreshToken)) {
            // 검증 실패 예외
            throw new AuthenticationFailureException("위조된 토큰입니다.");
        }

        // 2. 우리 서버에 존재하는 refresh token 인지 검증
        Optional<RefreshToken> optionalRt = refreshTokenRepository.findByToken(userRefreshToken);
        if(optionalRt.isEmpty()){
            throw new AuthenticationFailureException("로그아웃으로 인해 삭제된 토큰입니다.");
        }

        // 3. 만료 여부 확인
        Date expiration = tokenProvider.getExpiration(userRefreshToken);
        if(expiration.before(new Date())){
            // 만료기간 지난 예외
            throw new AuthenticationFailureException("더 이상 사용할 수 없는 토큰입니다.");
        }

        // 3. 만료 안됐으면 새로운 access token 만들어서 반환
        String username = tokenProvider.getUsernameFromToken(userRefreshToken);
        String accessToken = tokenProvider.generateAccessToken(username);

        return new RefreshResponse(accessToken);
    }
}
