package com.green_computer.green_board.scheduler;

import com.green_computer.green_board.repository.AccessTokenBlacklistRepository;
import com.green_computer.green_board.repository.RefreshTokenRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@AllArgsConstructor
@Slf4j
public class TokenCleanupScheduler {
    private final RefreshTokenRepository refreshTokenRepository;
    private final AccessTokenBlacklistRepository accessTokenBlacklistRepository;
    /*
    * 매일 새벽 03시에 'RefreshToken' 과 'AccessTokenBlacklist' 테이블 내
    * 만료기간이 지난 데이터를 물리 삭제하는 스케줄러
     */
    @Scheduled(cron = "0 0 3 * * *", zone = "Asia/Seoul")
    @Transactional
    public void cleanTokens(){
        LocalDateTime now = LocalDateTime.now();
        log.warn("토큰 정리 스케줄러가 실행됩니다. 실행 시작 시간 : {}", now);

        // 1. RefreshToken 테이블에서, 만료 시간이 지난 데이터들을 찾아서 모두 지운다.
        refreshTokenRepository.deleteAllByExpirationDateTimeBefore(now);

        // 2. AccessTokenBlacklist 테이블에서, 만료 시간이 지난 데이터들을 찾아서 모두 지운다.
        accessTokenBlacklistRepository.deleteAllByExpirationDateTimeBefore(now);

        log.warn("토큰 정리 스케줄러 작업이 성공적으로 마무리 되었습니다. 실행 시작 시간 : {}", now);
    }
}
