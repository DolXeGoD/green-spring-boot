package com.green_computer.green_board.controller;

import com.green_computer.green_board.dto.UserResponse;
import com.green_computer.green_board.dto.UserUpdateRequest;
import com.green_computer.green_board.service.UserService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {
    private final UserService userService;
    public UserController(UserService userService) {
        this.userService = userService;
    }

    // 타인 정보 조회
    @GetMapping("/{id}")
    public UserResponse getOtherUsersDetail(@PathVariable int id) {
        return userService.getOtherUsersDetail(id);
    }

    // 수정
    @PatchMapping("/me")
    public void updateUserInfo(
            @RequestBody UserUpdateRequest request
    ) {
        userService.updateUserInfo(request);
    }

    // 삭제
    @DeleteMapping("/me")
    public void deleteUser() {
        userService.deleteUser();
    }

}
