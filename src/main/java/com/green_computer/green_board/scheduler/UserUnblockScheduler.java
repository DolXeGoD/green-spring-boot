package com.green_computer.green_board.scheduler;

import com.green_computer.green_board.entity.User;
import com.green_computer.green_board.enums.UserStatus;
import com.green_computer.green_board.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;


@Component
@AllArgsConstructor
@Slf4j
public class UserUnblockScheduler {
    private final UserRepository userRepository;

    @Scheduled(cron = "0 0 0/1 1/1 * ?")
    @Transactional
    public void unblockUsers() {
        List<User> targetUsers = userRepository.findByStatusAndUnblockDateTimeBefore(UserStatus.BLOCKED, LocalDateTime.now());
        for(User user : targetUsers) {
            user.setStatus(UserStatus.ACTIVE);
            user.setUnblockDateTime(null);
            userRepository.save(user);
        }
    }
}
