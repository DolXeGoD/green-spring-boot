package com.green_computer.green_board.controller;

import com.green_computer.green_board.dto.ApiResponse;
import com.green_computer.green_board.dto.ReportCreateRequest;
import com.green_computer.green_board.service.ReportService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
@AllArgsConstructor
public class ReportController {
    private final ReportService reportService;

    @PostMapping("/boards/{boardId}")
    public ResponseEntity<ApiResponse<Void>> reportBoard(
            @PathVariable int boardId, @Valid @RequestBody ReportCreateRequest request
    ) {
        reportService.reportBoard(boardId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok());
    }

    @PostMapping("/comments/{commentId}")
    public ResponseEntity<ApiResponse<Void>> reportComment(
            @PathVariable int commentId, @Valid @RequestBody ReportCreateRequest request
    ) {
        reportService.reportComment(commentId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok());
    }
}
