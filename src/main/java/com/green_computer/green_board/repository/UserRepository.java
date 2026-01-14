package com.green_computer.green_board.repository;

import com.green_computer.green_board.entity.User;
import com.green_computer.green_board.enums.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    // JPA가 함수 이름을 분석함
    // find - 찾는다
    // ByUsername - username으로 찾는다
    // findByUsername -> WHERE에 username 걸어서 SELECT 하고 싶구나!
    User findByUsername(String username);
    Optional<User> findByEmail(String email);
    List<User> findByStatusAndUnblockDateTimeBefore(UserStatus status, LocalDateTime unblockDateTime);
}
