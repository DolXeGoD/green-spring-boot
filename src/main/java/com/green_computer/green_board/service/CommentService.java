package com.green_computer.green_board.service;

import com.green_computer.green_board.dto.CommentCreateRequest;
import com.green_computer.green_board.dto.CommentResponse;
import com.green_computer.green_board.entity.Board;
import com.green_computer.green_board.entity.Comment;
import com.green_computer.green_board.entity.User;
import com.green_computer.green_board.exceptions.AuthorizationFailureException;
import com.green_computer.green_board.exceptions.ResourceNotFoundException;
import com.green_computer.green_board.repository.BoardRepository;
import com.green_computer.green_board.repository.CommentRepository;
import com.green_computer.green_board.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;

    public void createComment(int boardId, CommentCreateRequest request) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResourceNotFoundException("게시글을 찾을 수 없습니다."));
        if (board.isDeleted()) {
            throw new ResourceNotFoundException("삭제된 게시글입니다.");
        }

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username);

        Comment comment = new Comment();
        comment.setBoard(board);
        comment.setAuthor(user);
        comment.setContent(request.getContent());
        commentRepository.save(comment);
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> getComments(int boardId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResourceNotFoundException("게시글을 찾을 수 없습니다."));
        if (board.isDeleted()) {
            throw new ResourceNotFoundException("삭제된 게시글입니다.");
        }

        List<CommentResponse> responses = new ArrayList<>();
        List<Comment> comments = commentRepository.findByBoardIdAndIsDeletedFalseOrderByIdAsc(boardId);
        for (Comment comment : comments) {
            responses.add(new CommentResponse(
                    comment.getId(), boardId, comment.getAuthor().getName(),
                    comment.getContent(), comment.getCreatedDatetime()
            ));
        }
        return responses;
    }

    @Transactional
    public void updateComment(int id, CommentCreateRequest request) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("댓글을 찾을 수 없습니다."));
        if (comment.isDeleted() || comment.getBoard().isDeleted()) {
            throw new ResourceNotFoundException("삭제된 댓글입니다.");
        }

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!comment.getAuthor().getUsername().equals(username)) {
            throw new AuthorizationFailureException("작성자 이외에는 댓글을 수정할 수 없습니다.");
        }

        comment.setContent(request.getContent());
        commentRepository.save(comment);
    }

    @Transactional
    public void deleteComment(int id) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("댓글을 찾을 수 없습니다."));
        if (comment.isDeleted()) {
            throw new ResourceNotFoundException("삭제된 댓글입니다.");
        }

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!comment.getAuthor().getUsername().equals(username)) {
            throw new AuthorizationFailureException("작성자 이외에는 댓글을 삭제할 수 없습니다.");
        }

        comment.setDeleted(true);
        commentRepository.save(comment);
    }
}
