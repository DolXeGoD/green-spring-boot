package com.green_computer.green_board.service;

import com.green_computer.green_board.dto.*;
import com.green_computer.green_board.entity.AccessTokenBlacklist;
import com.green_computer.green_board.entity.RefreshToken;
import com.green_computer.green_board.entity.User;
import com.green_computer.green_board.entity.VerificationCode;
import com.green_computer.green_board.enums.UserRole;
import com.green_computer.green_board.enums.UserStatus;
import com.green_computer.green_board.exceptions.AuthenticationFailureException;
import com.green_computer.green_board.exceptions.InvalidStateException;
import com.green_computer.green_board.exceptions.ResourceNotFoundException;
import com.green_computer.green_board.security.JwtTokenProvider;
import com.green_computer.green_board.repository.AccessTokenBlacklistRepository;
import com.green_computer.green_board.repository.RefreshTokenRepository;
import com.green_computer.green_board.repository.UserRepository;
import com.green_computer.green_board.repository.VerificationCodeRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;
import java.util.Random;

@Service
@Slf4j
@AllArgsConstructor
public class AuthService {
    private final AccessTokenBlacklistRepository accessTokenBlacklistRepository;
    private UserRepository userRepository;
    private RefreshTokenRepository refreshTokenRepository;
    private VerificationCodeRepository verificationCodeRepository;
    private PasswordEncoder passwordEncoder;
    private JwtTokenProvider tokenProvider;
    private EmailService emailService;
    private AuthenticationManager authenticationManager;

    @Transactional
    public LoginResponse login(UserLoginRequest userLoginRequest) {
        // 엑세스토큰, 리프레시 토큰
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    userLoginRequest.getUsername(), userLoginRequest.getPassword()
            ));
        } catch (AuthenticationException exception) {
            throw new AuthenticationFailureException("아이디 또는 비밀번호가 올바르지 않습니다.");
        }

        User user = userRepository.findByUsername(userLoginRequest.getUsername());

        // 로그인 성공
        String accessToken = tokenProvider.generateAccessToken(user.getUsername());
        String refreshToken = tokenProvider.generateRefreshToken(user.getUsername());

        refreshTokenRepository.deleteByUserId(user.getId());

        Date expiration = tokenProvider.getExpiration(refreshToken);
        LocalDateTime expirationDateTime = expiration.toInstant()
                .atZone(ZoneId.systemDefault()).toLocalDateTime();

        RefreshToken refresh = new RefreshToken();
        refresh.setToken(refreshToken);
        refresh.setUser(user);
        refresh.setExpirationDateTime(expirationDateTime);
        refreshTokenRepository.save(refresh);

        return new LoginResponse(accessToken, refreshToken);
    }

    @Transactional
    public void register(UserRegisterRequest userRegisterRequest) {
        if (userRepository.findByUsername(userRegisterRequest.getUsername()) != null) {
            throw new InvalidStateException("이미 사용 중인 아이디입니다.");
        }
        if (userRepository.findByEmail(userRegisterRequest.getEmail()).isPresent()) {
            throw new InvalidStateException("이미 사용 중인 이메일입니다.");
        }
        // 유저 가입 with PENDING
        String encodedPassword = passwordEncoder.encode(userRegisterRequest.getPassword());
        User user = new User();
        user.setUsername(userRegisterRequest.getUsername());
        user.setPassword(encodedPassword);
        user.setEmail(userRegisterRequest.getEmail());
        user.setName(userRegisterRequest.getName());
        user.setRole(UserRole.USER);
        user.setStatus(UserStatus.PENDING);

        userRepository.save(user);

        // 인증번호 발송
        String verificationCode = generateVerificationCode();
        VerificationCode verification = VerificationCode.builder()
                .user(user)
                .code(verificationCode)
                .expirationDatetime(LocalDateTime.now().plusMinutes(10))
                .build();

        verificationCodeRepository.save(verification);

        emailService.sendVerificationCode(
                userRegisterRequest.getEmail(),
                verificationCode
        );
    }

    @Transactional
    public void logout(LogoutRequest logoutRequest) {
        // 행동 : 로그아웃 할 사용자의 리프레시 토큰을 DB에서 지운다.

        // 1. 지금 로그아웃을 요청한 사용자의 유저 네임을 알아낸다
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        String accessToken = logoutRequest.getAccessToken();
        if (!tokenProvider.validateAccessToken(accessToken)) {
            throw new AuthenticationFailureException("유효한 엑세스 토큰이 아닙니다.");
        }
        if (!username.equals(tokenProvider.getUsernameFromToken(accessToken))) {
            throw new AuthenticationFailureException("본인의 토큰만 로그아웃할 수 있습니다.");
        }
        // 2. 사용자의 유저네임을 통해 사용자의 id (pk) 를 알아낸다
        int userId = userRepository.findByUsername(username).getId();
        // 3. refresh_token 테이블에서 해당 사용자의 모든 refresh token을 찾아 지운다.
        refreshTokenRepository.deleteByUserId(userId);

        // 행동 : 로그아웃 할 사용자의 엑세스토큰을 블랙리스트에 삽입한다.

        // 1. 지금 로그아웃을 요청한 사용자의 엑세스 토큰을 가져온다
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
        if(!tokenProvider.validateRefreshToken(userRefreshToken)) {
            // 검증 실패 예외
            throw new AuthenticationFailureException("만료되었거나 유효하지 않은 리프레시 토큰입니다.");
        }

        // 2. 우리 서버에 존재하는 refresh token 인지 검증
        Optional<RefreshToken> optionalRt = refreshTokenRepository.findByToken(userRefreshToken);
        if(optionalRt.isEmpty()){
            throw new AuthenticationFailureException("로그아웃으로 인해 삭제된 토큰입니다.");
        }

        if (!optionalRt.get().getExpirationDateTime().isAfter(LocalDateTime.now())) {
            throw new AuthenticationFailureException("만료된 리프레시 토큰입니다.");
        }

        // 만료되지 않은 토큰이면 새로운 access token을 만들어서 반환
        String username = tokenProvider.getUsernameFromToken(userRefreshToken);
        User user = userRepository.findByUsername(username);
        if (user == null || user.isDeleted() || user.getStatus() != UserStatus.ACTIVE) {
            throw new AuthenticationFailureException("사용할 수 없는 계정입니다.");
        }
        String accessToken = tokenProvider.generateAccessToken(username);

        return new RefreshResponse(accessToken);
    }

    @Transactional
    public void verifyRegister(VerifyRegisterRequest verifyRegisterRequest) {
        // email로 id 찾기
        User user = userRepository.findByEmail(verifyRegisterRequest.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("유저를 찾을 수 없습니다."));

        VerificationCode verificationCode = verificationCodeRepository.findByUserIdAndExpirationDatetimeAfterAndIsVerifiedFalse(user.getId(), LocalDateTime.now())
                .orElseThrow(() -> new ResourceNotFoundException("인증 시도를 하지 않은 이메일입니다."));

        if(!verifyRegisterRequest.getCode().equals(verificationCode.getCode())) {
            throw new AuthenticationFailureException("인증번호가 다릅니다.");
        }

        if(verificationCode.getExpirationDatetime().isBefore(LocalDateTime.now())) {
            throw new AuthenticationFailureException("인증 시간이 만료되었습니다.");
        }

        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);
        verificationCode.setVerified(true);
        verificationCodeRepository.save(verificationCode);
        log.info("ACTIVE 처리 완료. 유저 이메일: {}", user.getEmail());
    }

    private String generateVerificationCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }
}
