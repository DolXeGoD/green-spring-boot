package com.green_computer.green_board;

import com.green_computer.green_board.entity.User;
import com.green_computer.green_board.enums.UserStatus;
import com.green_computer.green_board.repository.AccessTokenBlacklistRepository;
import com.green_computer.green_board.repository.RefreshTokenRepository;
import com.green_computer.green_board.repository.UserRepository;
import com.green_computer.green_board.scheduler.TokenCleaningScheduler;
import com.green_computer.green_board.scheduler.UserUnblockScheduler;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class SchedulerTests {
    @Test
    void removesExpiredTokensUsingTheSameTime() {
        RefreshTokenRepository refreshTokens = mock(RefreshTokenRepository.class);
        AccessTokenBlacklistRepository blacklist = mock(AccessTokenBlacklistRepository.class);
        TokenCleaningScheduler scheduler = new TokenCleaningScheduler(refreshTokens, blacklist);

        scheduler.cleanTokens();

        ArgumentCaptor<LocalDateTime> time = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(refreshTokens).deleteAllByExpirationDateTimeBefore(time.capture());
        verify(blacklist).deleteAllByExpirationDateTimeBefore(time.getValue());
    }

    @Test
    void restoresUsersWhoseBlockHasExpired() {
        UserRepository users = mock(UserRepository.class);
        User user = new User();
        user.setStatus(UserStatus.BLOCKED);
        user.setUnblockDateTime(LocalDateTime.now().minusMinutes(1));
        when(users.findByStatusAndUnblockDateTimeBefore(eq(UserStatus.BLOCKED), any(LocalDateTime.class)))
                .thenReturn(List.of(user));
        UserUnblockScheduler scheduler = new UserUnblockScheduler(users);

        scheduler.unblockUsers();

        assertEquals(UserStatus.ACTIVE, user.getStatus());
        assertNull(user.getUnblockDateTime());
        verify(users).save(user);
    }
}
