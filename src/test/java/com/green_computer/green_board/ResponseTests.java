package com.green_computer.green_board;

import com.green_computer.green_board.controller.BoardController;
import com.green_computer.green_board.controller.UserController;
import com.green_computer.green_board.dto.PostResponse;
import com.green_computer.green_board.dto.ApiResponse;
import com.green_computer.green_board.entity.Board;
import com.green_computer.green_board.entity.User;
import com.green_computer.green_board.enums.BoardType;
import com.green_computer.green_board.repository.BoardRepository;
import com.green_computer.green_board.repository.LikeRepository;
import com.green_computer.green_board.repository.UserRepository;
import com.green_computer.green_board.service.BoardService;
import com.green_computer.green_board.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ResponseTests {
    @Test
    void ordinaryQueryKeepsTheFullContentAndResponseFields() {
        BoardRepository boards = mock(BoardRepository.class);
        BoardService service = new BoardService(boards, mock(UserRepository.class), mock(LikeRepository.class));
        User author = new User();
        author.setName("작성자");
        Board board = new Board();
        board.setId(1);
        board.setTitle("게시글 제목");
        board.setContent("본문".repeat(100));
        board.setAuthor(author);
        board.setHits(12);
        board.setLikeCount(3);
        board.setCreatedDatetime(LocalDateTime.of(2026, 9, 27, 10, 0));
        board.setUpdatedDatetime(LocalDateTime.of(2026, 9, 28, 11, 0));
        when(boards.findByIsDeletedFalseAndTypeOrderByIdDesc(BoardType.GENERAL)).thenReturn(List.of(board));

        PostResponse response = service.getAllBoardsWithoutPreview().get(0);

        assertEquals(board.getContent(), response.getContent());
        assertEquals(12, response.getHits());
        assertEquals(3, response.getLikeCount());
        assertEquals(board.getCreatedDatetime(), response.getCreatedDatetime());
        assertEquals(board.getUpdatedDatetime(), response.getUpdatedDatetime());
    }

    @Test
    void detailReturnsTheUpdatedDateAfterSaving() {
        BoardRepository boards = mock(BoardRepository.class);
        BoardService service = new BoardService(boards, mock(UserRepository.class), mock(LikeRepository.class));
        User author = new User();
        author.setName("작성자");
        Board board = new Board();
        board.setAuthor(author);
        LocalDateTime updated = LocalDateTime.of(2026, 9, 28, 12, 0);
        when(boards.findById(1)).thenReturn(Optional.of(board));
        when(boards.saveAndFlush(board)).thenAnswer(invocation -> {
            board.setUpdatedDatetime(updated);
            return board;
        });

        PostResponse response = service.getDetailPost(1);

        assertEquals(1, response.getHits());
        assertEquals(updated, response.getUpdatedDatetime());
        verify(boards).saveAndFlush(board);
    }

    @Test
    void boardDeletionReturns204WithoutABody() {
        BoardService service = mock(BoardService.class);
        BoardController controller = new BoardController(service);

        ResponseEntity<ApiResponse<Void>> response = controller.deletePost(1);

        assertEquals(204, response.getStatusCode().value());
        assertNull(response.getBody());
        verify(service).deletePost(1);
    }

    @Test
    void userDeletionReturns204WithoutABody() {
        UserService service = mock(UserService.class);
        UserController controller = new UserController(service);

        ResponseEntity<ApiResponse<Void>> response = controller.deleteUser();

        assertEquals(204, response.getStatusCode().value());
        assertNull(response.getBody());
        verify(service).deleteUser();
    }
}
