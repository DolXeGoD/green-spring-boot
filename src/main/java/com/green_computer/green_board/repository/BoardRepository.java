package com.green_computer.green_board.repository;

import com.green_computer.green_board.entity.Board;
import com.green_computer.green_board.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BoardRepository extends JpaRepository<Board, Integer> {
    List<Board> findBoardsByIsDeletedFalse(Pageable pageable);
    List<Board> findBoardsByAuthor(User user);
}
