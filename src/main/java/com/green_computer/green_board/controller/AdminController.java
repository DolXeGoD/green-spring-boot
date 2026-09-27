package com.green_computer.green_board.controller;

import com.green_computer.green_board.dto.AdminUserBlockRequest;
import com.green_computer.green_board.dto.ApiResponse;
import com.green_computer.green_board.dto.ReportProcessRequest;
import com.green_computer.green_board.dto.ReportResponse;
import com.green_computer.green_board.service.AdminService;
import com.green_computer.green_board.service.ReportService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@AllArgsConstructor
public class AdminController {

    private AdminService adminService;
    private ReportService reportService;

    @PostMapping("/user/{id}/ban")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> banUser(@PathVariable int id){
        adminService.banUser(id);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    @PostMapping("/user/{id}/block")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> blockUser(@PathVariable int id, @Valid @RequestBody AdminUserBlockRequest request){
        adminService.blockUser(id, request);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    @DeleteMapping("/user/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable int id) {
        adminService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/boards/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteBoard(@PathVariable int id) {
        adminService.deleteBoard(id);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    @DeleteMapping("/comments/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteComment(@PathVariable int id) {
        adminService.deleteComment(id);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    @GetMapping("/reports")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<List<ReportResponse>>> getReports() {
        return ResponseEntity.ok(ApiResponse.ok(reportService.getReports()));
    }

    @PatchMapping("/reports/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> processReport(
            @PathVariable int id, @Valid @RequestBody ReportProcessRequest request
    ) {
        reportService.processReport(id, request);
        return ResponseEntity.ok(ApiResponse.ok());
    }
}
