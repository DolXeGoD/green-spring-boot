package com.green_computer.green_board.service;

import com.green_computer.green_board.entity.User;
import com.green_computer.green_board.enums.UserRole;
import com.green_computer.green_board.enums.UserStatus;
import com.green_computer.green_board.exceptions.AuthorizationFailureException;
import com.green_computer.green_board.exceptions.ResourceNotFoundException;
import com.green_computer.green_board.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class AdminService {
    private final UserRepository userRepository;

    public void banUser(int id){
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

//        user.setStatus(UserStatus.BANNED);
//        userRepository.save(user);

        log.info("유저 {} 밴 처리 완료", user.getUsername());
    }

}
