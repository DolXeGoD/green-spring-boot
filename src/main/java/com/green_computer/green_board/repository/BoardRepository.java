package com.green_computer.green_board.repository;

import com.green_computer.green_board.entity.Board;
import com.green_computer.green_board.entity.User;
import com.green_computer.green_board.enums.BoardType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BoardRepository extends JpaRepository<Board, Integer> {
    List<Board> findByAuthorAndIsDeletedFalseOrderByCreatedDatetimeDescIdDesc(User user);
    Page<Board> findByIsDeletedFalseAndType(BoardType type, Pageable pageable);

    @Query(value = "SELECT * FROM boards WHERE is_deleted = false AND MATCH(title) AGAINST(:keyword)", nativeQuery = true)
    List<Board> searchByTitle(@Param("keyword") String keyword);
}
