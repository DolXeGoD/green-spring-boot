package com.green_computer.green_board.controller;

import com.green_computer.green_board.dto.ApiResponse;
import com.green_computer.green_board.dto.UserResponse;
import com.green_computer.green_board.dto.UserUpdateRequest;
import com.green_computer.green_board.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<ApiResponse<UserResponse>> getOtherUsersDetail(@PathVariable int id) {
        return ResponseEntity.ok(ApiResponse.ok(userService.getOtherUsersDetail(id)));
    }

    // 내 정보 수정
    @PatchMapping("/me")
    public ResponseEntity<ApiResponse<Void>> updateUserInfo(
            @RequestBody UserUpdateRequest request
    ) {
        userService.updateUserInfo(request);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    // 탈퇴
    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<Void>> deleteUser() {
        userService.deleteUser();
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(ApiResponse.ok());
    }
}
