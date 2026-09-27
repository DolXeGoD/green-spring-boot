package com.green_computer.green_board.controller;

import com.green_computer.green_board.dto.ApiResponse;
import com.green_computer.green_board.dto.PostCreateRequest;
import com.green_computer.green_board.dto.PostResponse;
import com.green_computer.green_board.dto.PostUpdateRequest;
import com.green_computer.green_board.dto.BoardListResponse;
import com.green_computer.green_board.enums.BoardType;
import com.green_computer.green_board.service.BoardService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    public ResponseEntity<ApiResponse<Page<PostResponse>>> getAllBoards(
            @PageableDefault(sort = {"createdDatetime", "id"}, direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<PostResponse> results = boardService.getAllBoards(pageable);
        return ResponseEntity.ok(ApiResponse.ok(results)); // 200 OK with 글 데이터들
    }

    @GetMapping("/home")
    public ResponseEntity<ApiResponse<BoardListResponse>> getBoardHome(
            @PageableDefault(sort = {"createdDatetime", "id"}, direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.ok(boardService.getBoardHome(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PostResponse>> getDetail(@PathVariable int id) {
        PostResponse response = boardService.getDetailPost(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    // 3. 새로운 글 작성
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createNewPost(@Valid @RequestBody PostCreateRequest request) {
        int newPostId = boardService.createNewPost(request, BoardType.GENERAL);
        URI location = URI.create("/api/board/" + newPostId);
        return ResponseEntity.created(location).body(ApiResponse.ok());
    }

    @PostMapping("/notices")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> createNotice(@Valid @RequestBody PostCreateRequest request) {
        int newPostId = boardService.createNewPost(request, BoardType.NOTICE);
        URI location = URI.create("/api/board/" + newPostId);
        return ResponseEntity.created(location).body(ApiResponse.ok());
    }

    // 4. 수정
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> updatePost(
            @PathVariable int id,
            @Valid @RequestBody PostUpdateRequest request
    ){
        boardService.updatePost(id, request);
        return ResponseEntity.ok().body(ApiResponse.ok());
    }

    // 5. 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePost(@PathVariable int id) {
        boardService.deletePost(id);
        return ResponseEntity.noContent().build();
    }

    // 내가 작성한 글 조회
    @GetMapping("/my-posts")
    public ResponseEntity<ApiResponse<List<PostResponse>>> getMyPosts() {
        return ResponseEntity.ok(ApiResponse.ok(boardService.getMyPosts()));
    }

    @PostMapping("/likes/{id}")
    public ResponseEntity<ApiResponse<Boolean>> toggleLike(@PathVariable int id) {
        boolean like = boardService.toggleLike(id);
        return ResponseEntity.ok(ApiResponse.ok(like));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<PostResponse>>> searchPosts(@RequestParam String keyword) {
        List<PostResponse> response = boardService.search(keyword);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
