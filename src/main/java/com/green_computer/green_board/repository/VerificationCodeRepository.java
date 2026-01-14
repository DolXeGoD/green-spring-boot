package com.green_computer.green_board.repository;

import com.green_computer.green_board.entity.VerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.swing.text.html.Option;
import java.time.LocalDateTime;
import java.util.Optional;

public interface VerificationCodeRepository extends JpaRepository<VerificationCode, Integer> {
    Optional<VerificationCode> findByUserIdAndExpirationDatetimeAfterAndIsVerifiedFalse(Integer userId, LocalDateTime expirationDateTime);
}
