package com.green_computer.green_board.repository;

import com.green_computer.green_board.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Integer> {
    void deleteByUserId(int userId);
    Optional<RefreshToken> findByToken(String token);

    // expirationDatetime 이 지금 시간보다 이전인 모든 데이터를 삭제한다
    void deleteAllByExpirationDateTimeBefore(LocalDateTime dateTime);
}
