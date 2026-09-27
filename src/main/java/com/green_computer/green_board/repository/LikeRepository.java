package com.green_computer.green_board.repository;

import com.green_computer.green_board.entity.Like;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface LikeRepository extends JpaRepository<Like, Integer> {
    Optional<Like> findByUserIdAndBoardId(int userId, int boardId);
    List<Like> findByUserId(int userId);
}
