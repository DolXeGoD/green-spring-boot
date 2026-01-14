package com.green_computer.green_board.controller;

import com.green_computer.green_board.dto.*;
import com.green_computer.green_board.exceptions.AuthenticationFailureException;
import com.green_computer.green_board.service.AuthService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody UserLoginRequest userLoginRequest,
            HttpSession session
    ) {
        LoginResponse result = authService.login(userLoginRequest, session);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> register(@Valid @RequestBody UserRegisterRequest userRegisterRequest) {
        authService.register(userRegisterRequest);
        return ResponseEntity.ok().body(ApiResponse.ok());
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@Valid @RequestBody LogoutRequest logoutRequest) {
        authService.logout(logoutRequest);
        return ResponseEntity.ok().body(ApiResponse.ok());
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<RefreshResponse>> refresh(@Valid @RequestBody RefreshRequest refreshRequest) {
        RefreshResponse response = authService.refresh(refreshRequest);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/verify-register")
    public ResponseEntity<ApiResponse<Void>> verifyRegister(
            @Valid @RequestBody VerifyRegisterRequest verifyRegisterRequest
    ) {
        authService.verifyRegister(verifyRegisterRequest);
        return ResponseEntity.ok(ApiResponse.ok());
    }
}
