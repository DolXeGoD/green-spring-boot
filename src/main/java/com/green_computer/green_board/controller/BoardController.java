package com.green_computer.green_board.controller;

import com.green_computer.green_board.dto.ApiResponse;
import com.green_computer.green_board.dto.PostCreateRequest;
import com.green_computer.green_board.dto.PostResponse;
import com.green_computer.green_board.dto.PostUpdateRequest;
import com.green_computer.green_board.entity.Board;
import com.green_computer.green_board.service.BoardService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/board")
public class BoardController {
    private final BoardService boardService;

    public BoardController(BoardService boardService) {
        this.boardService = boardService;
    }


    // 1. 모든 게시글을, 작성 최신순으로 조회.
    @GetMapping
    public ResponseEntity<ApiResponse<List<PostResponse>>> getAllBoards(Pageable pageable) {
        List<PostResponse> results = boardService.getAllBoards(pageable);
        return ResponseEntity.ok(ApiResponse.ok(results)); // 200 OK with 글 데이터들
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PostResponse>> getDetail(@PathVariable int id) {
        PostResponse response = boardService.getDetailPost(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    // 3. 새로운 글 작성
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createNewPost(@Valid @RequestBody PostCreateRequest request) {
        int newPostId = boardService.createNewPost(request);
        URI location = URI.create("/getDetail/" + newPostId);
        return ResponseEntity.created(location).body(ApiResponse.ok());
    }

    // 4. 수정
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> updatePost(
            @PathVariable int id,
            @RequestBody PostUpdateRequest request
    ){
        boardService.updatePost(id, request);
        return ResponseEntity.ok().body(ApiResponse.ok());
    }

    // 5. 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePost(@PathVariable int id) {
        boardService.deletePost(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(ApiResponse.ok());
    }

    // 내가 작성한 글 조회
    @GetMapping("/my-posts")
    public ResponseEntity<ApiResponse<List<PostResponse>>> getMyPosts() {
        return ResponseEntity.ok(ApiResponse.ok(boardService.getMyPosts()));
    }
}
