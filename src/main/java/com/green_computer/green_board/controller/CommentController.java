package com.green_computer.green_board.controller;

import com.green_computer.green_board.dto.ApiResponse;
import com.green_computer.green_board.dto.CommentCreateRequest;
import com.green_computer.green_board.dto.CommentResponse;
import com.green_computer.green_board.service.CommentService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class CommentController {
    private final CommentService commentService;

    @GetMapping("/board/{boardId}/comments")
    public ResponseEntity<ApiResponse<List<CommentResponse>>> getComments(@PathVariable int boardId) {
        return ResponseEntity.ok(ApiResponse.ok(commentService.getComments(boardId)));
    }

    @PostMapping("/board/{boardId}/comments")
    public ResponseEntity<ApiResponse<Void>> createComment(
            @PathVariable int boardId, @Valid @RequestBody CommentCreateRequest request
    ) {
        commentService.createComment(boardId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok());
    }

    @PatchMapping("/comments/{id}")
    public ResponseEntity<ApiResponse<Void>> updateComment(
            @PathVariable int id, @Valid @RequestBody CommentCreateRequest request
    ) {
        commentService.updateComment(id, request);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    @DeleteMapping("/comments/{id}")
    public ResponseEntity<Void> deleteComment(@PathVariable int id) {
        commentService.deleteComment(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
