package com.green_computer.green_board.repository;

import com.green_computer.green_board.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer> {
    // JPA가 함수 이름을 분석함
    // find - 찾는다
    // ByUsername - username으로 찾는다
    // findByUsername -> WHERE에 username 걸어서 SELECT 하고 싶구나!
    User findByUsername(String username);
}
