package com.green_computer.green_board.service;

import com.green_computer.green_board.dto.UserResponse;
import com.green_computer.green_board.dto.UserUpdateRequest;
import com.green_computer.green_board.dto.CommentResponse;
import com.green_computer.green_board.dto.PasswordChangeRequest;
import com.green_computer.green_board.dto.PostResponse;
import com.green_computer.green_board.entity.Comment;
import com.green_computer.green_board.entity.Like;
import com.green_computer.green_board.entity.User;
import com.green_computer.green_board.enums.UserStatus;
import com.green_computer.green_board.exceptions.AuthenticationFailureException;
import com.green_computer.green_board.exceptions.ResourceNotFoundException;
import com.green_computer.green_board.repository.CommentRepository;
import com.green_computer.green_board.repository.LikeRepository;
import com.green_computer.green_board.repository.UserRepository;
import com.green_computer.green_board.repository.RefreshTokenRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final LikeRepository likeRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;

    public UserService(UserRepository userRepository, CommentRepository commentRepository,
                       LikeRepository likeRepository, PasswordEncoder passwordEncoder,
                       RefreshTokenRepository refreshTokenRepository) {
        this.userRepository = userRepository;
        this.commentRepository = commentRepository;
        this.likeRepository = likeRepository;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public UserResponse getOtherUsersDetail(int id) {
        // 사용자가 준 타인 ID로 유저를 조회해서, 닉네임, 가입일 내릴것이다.
        Optional<User> targetUserOptional = userRepository.findById(id);
        if(targetUserOptional.isEmpty()) {
           throw new ResourceNotFoundException("유저를 찾을 수 없습니다.");
        }

        User targetUser = targetUserOptional.get();
        if(targetUser.isDeleted()) {
            throw new ResourceNotFoundException("탈퇴한 유저입니다.");
        }

        UserResponse ur = new UserResponse(
                targetUser.getId(),
                targetUser.getName(),
                targetUser.getCreatedDateTime(),
                targetUser.getUpdatedDateTime()
        );

        return ur;
    }

    // 내 정보 수정
    public void updateUserInfo(UserUpdateRequest request) {
        User requestUser = userRepository.findByUsername(
                SecurityContextHolder.getContext().getAuthentication().getName()
        );

        if(request.getName() != null){
            requestUser.setName(request.getName());
            userRepository.save(requestUser);
        }
    }

    @Transactional
    public void deleteUser() {
        User requestUser = userRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
        requestUser.setDeleted(true);
        requestUser.setStatus(UserStatus.QUITTED);
        requestUser.setUnblockDateTime(null);
        userRepository.save(requestUser);
        refreshTokenRepository.deleteByUserId(requestUser.getId());
    }

    @Transactional
    public void changePassword(PasswordChangeRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username);

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new AuthenticationFailureException("현재 비밀번호가 올바르지 않습니다.");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        refreshTokenRepository.deleteByUserId(user.getId());
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> getMyComments() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username);
        List<Comment> comments = commentRepository.findByAuthorIdAndIsDeletedFalseOrderByIdDesc(user.getId());
        List<CommentResponse> responses = new ArrayList<>();

        for (Comment comment : comments) {
            if (!comment.getBoard().isDeleted()) {
                responses.add(new CommentResponse(
                        comment.getId(), comment.getBoard().getId(), user.getName(),
                        comment.getContent(), comment.getCreatedDatetime()
                ));
            }
        }
        return responses;
    }

    @Transactional(readOnly = true)
    public List<PostResponse> getMyLikes() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username);
        List<Like> likes = likeRepository.findByUserId(user.getId());
        List<PostResponse> responses = new ArrayList<>();

        for (Like like : likes) {
            if (!like.getBoard().isDeleted()) {
                responses.add(new PostResponse(
                        like.getBoard().getId(), like.getBoard().getTitle(),
                        like.getBoard().getContent(), like.getBoard().getAuthor().getName(),
                        like.getBoard().getHits(), like.getBoard().getLikeCount(),
                        like.getBoard().getCreatedDatetime(), like.getBoard().getUpdatedDatetime()
                ));
            }
        }
        return responses;
    }
}
