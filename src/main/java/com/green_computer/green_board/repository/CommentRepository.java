package com.green_computer.green_board.repository;

import com.green_computer.green_board.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Integer> {
    List<Comment> findByBoardIdAndIsDeletedFalseOrderByIdAsc(int boardId);
    List<Comment> findByAuthorIdAndIsDeletedFalseOrderByIdDesc(int authorId);
}
