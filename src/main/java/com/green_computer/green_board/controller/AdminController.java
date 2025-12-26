package com.green_computer.green_board.controller;

import com.green_computer.green_board.dto.ApiResponse;
import com.green_computer.green_board.service.AdminService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@AllArgsConstructor
public class AdminController {

    private AdminService adminService;

    @PostMapping("/user/{id}/ban")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> banUser(@PathVariable int id){
        adminService.banUser(id);
        return ResponseEntity.ok(ApiResponse.ok());
    }
}
