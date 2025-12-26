package com.green_computer.green_board.repository;

import com.green_computer.green_board.entity.AccessTokenBlacklist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface AccessTokenBlacklistRepository extends JpaRepository<AccessTokenBlacklist, Integer> {
    Optional<AccessTokenBlacklist> findByToken(String token);

    void deleteAllByExpirationDateTimeBefore(LocalDateTime dateTime);
}

