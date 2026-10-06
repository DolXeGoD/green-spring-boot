package com.green_computer.green_board.security;

import com.green_computer.green_board.entity.User;
import com.green_computer.green_board.enums.UserStatus;
import com.green_computer.green_board.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username);

        if(user == null) {
            throw new UsernameNotFoundException(username);
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new UsernameNotFoundException("사용할 수 없는 계정입니다.");
        }

        return org.springframework.security.core.userdetails.User
                .builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(user.getRole().toString())
                .build();
    }
}
