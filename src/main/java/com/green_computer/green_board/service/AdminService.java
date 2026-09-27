package com.green_computer.green_board.service;

import com.green_computer.green_board.dto.AdminUserBlockRequest;
import com.green_computer.green_board.entity.Board;
import com.green_computer.green_board.entity.Comment;
import com.green_computer.green_board.entity.User;
import com.green_computer.green_board.enums.UserRole;
import com.green_computer.green_board.enums.UserStatus;
import com.green_computer.green_board.exceptions.AuthorizationFailureException;
import com.green_computer.green_board.exceptions.InvalidStateException;
import com.green_computer.green_board.exceptions.ResourceNotFoundException;
import com.green_computer.green_board.repository.UserRepository;
import com.green_computer.green_board.repository.BoardRepository;
import com.green_computer.green_board.repository.CommentRepository;
import com.green_computer.green_board.repository.RefreshTokenRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class AdminService {
    private final UserRepository userRepository;
    private final BoardRepository boardRepository;
    private final CommentRepository commentRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public void banUser(int id){
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if(!user.getStatus().equals(UserStatus.ACTIVE)){
            throw new InvalidStateException("이미 ACTIVE가 아닌 유저는 밴 할 수 없습니다.");
        }

        if(user.getRole().equals(UserRole.ADMIN)){
            throw new AuthorizationFailureException("ADMIN 유저는 밴 할 수 없습니다.");
        }

        user.setStatus(UserStatus.BANNED);
        userRepository.save(user);
        refreshTokenRepository.deleteByUserId(user.getId());

        log.info("유저 {} 밴 처리 완료", user.getUsername());
    }

    @Transactional
    public void blockUser(int id, AdminUserBlockRequest request){
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if(!user.getStatus().equals(UserStatus.ACTIVE)){
            throw new InvalidStateException("이미 ACTIVE가 아닌 유저는 차단 할 수 없습니다.");
        }

        if(user.getRole().equals(UserRole.ADMIN)){
            throw new AuthorizationFailureException("ADMIN 유저는 차단 할 수 없습니다.");
        }

        user.setStatus(UserStatus.BLOCKED);
        user.setUnblockDateTime(request.getBlockDatetime());

        userRepository.save(user);
        refreshTokenRepository.deleteByUserId(user.getId());

        log.info("유저 {} 일시차단 처리 완료", user.getUsername());
    }

    @Transactional
    public void deleteUser(int id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("유저를 찾을 수 없습니다."));

        if (user.isDeleted()) {
            throw new InvalidStateException("이미 탈퇴한 유저입니다.");
        }
        if (user.getRole() == UserRole.ADMIN) {
            throw new AuthorizationFailureException("ADMIN 유저는 강제 탈퇴시킬 수 없습니다.");
        }

        user.setDeleted(true);
        user.setStatus(UserStatus.QUITTED);
        user.setUnblockDateTime(null);
        userRepository.save(user);
        refreshTokenRepository.deleteByUserId(user.getId());
        log.info("유저 {} 강제 탈퇴 처리 완료", user.getUsername());
    }

    public void deleteBoard(int id) {
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("게시글을 찾을 수 없습니다."));
        board.setDeleted(true);
        boardRepository.save(board);
    }

    public void deleteComment(int id) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("댓글을 찾을 수 없습니다."));
        comment.setDeleted(true);
        commentRepository.save(comment);
    }

}
