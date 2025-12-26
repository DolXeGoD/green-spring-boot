package com.green_computer.green_board.controller;

import com.green_computer.green_board.dto.PostCreateRequest;
import com.green_computer.green_board.dto.PostResponse;
import com.green_computer.green_board.dto.PostUpdateRequest;
import com.green_computer.green_board.entity.Board;
import com.green_computer.green_board.service.BoardService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
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
    public ResponseEntity<?> getAllBoards() {
        List<PostResponse> results = boardService.getAllBoards();
        return ResponseEntity.ok(results); // 200 OK with 글 데이터들
    }

    // 2. 상세 조회. 글 하나를 클릭했을 때, 그 글의 상세정보 줘야함.
    // - Get 인 경우 RequestBody 사용하면 안됨 ! 강제는 아니지만 규칙임(RESTful).
    // - 그럼 데이터를 어떻게 보내야 되냐? URL에 포함시켜서 보낸다. ex) green.tistory.com/81
    // - /81, /80, /999 바뀔 수 있음. 그래서 URL은 특정 숫자가 아니라 {id} 로 설정
    // - PathVariable 설정까지 해주면, 스프링이 알아서 URL에서 숫자 뽑아서 int id에 갖다줌.
    @GetMapping("/{id}")
    public ResponseEntity<?> getDetail(@PathVariable int id) {
//        PostResponse response = boardService.getDetailPost(id);
        Board board = boardService.getDetailPost(id);
        return ResponseEntity.ok(board);
    }

    // 3. 새로운 글 작성
    @PostMapping
    public ResponseEntity<String> createNewPost(@Valid @RequestBody PostCreateRequest request) {
        int newPostId = boardService.createNewPost(request);
        URI location = URI.create("/getDetail/" + newPostId);
        return ResponseEntity.created(location).build();
    }

    // 4. 수정
    @PatchMapping("/{id}")
    public ResponseEntity<?> updatePost(
            // 어떤 게시글을 수정하고 싶은지는 PathVariable로
            @PathVariable int id,
            // 데이터 수정은 RequestBody로
            @RequestBody PostUpdateRequest request
    ){
        boardService.updatePost(id, request);
        return ResponseEntity.ok().build();
    }

    // 5. 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePost(@PathVariable int id) {
        boardService.deletePost(id);
        return ResponseEntity.noContent().build(); // 204 코드를 반환한다.
    }

    // 내가 작성한 글 조회
    @GetMapping("/my-posts")
    public ResponseEntity<?> getMyPosts() {
        return ResponseEntity.ok(boardService.getMyPosts());
    }
}
