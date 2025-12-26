package com.green_computer.green_board.service;

import com.green_computer.green_board.dto.UserResponse;
import com.green_computer.green_board.dto.UserUpdateRequest;
import com.green_computer.green_board.entity.User;
import com.green_computer.green_board.exceptions.ResourceNotFoundException;
import com.green_computer.green_board.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
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
                targetUser.getCreatedDateTime()
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

     public void deleteUser() {
         User requestUser = userRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
        requestUser.setDeleted(true);
        userRepository.save(requestUser);
     }
}
