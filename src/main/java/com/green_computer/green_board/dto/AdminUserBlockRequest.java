package com.green_computer.green_board.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class AdminUserBlockRequest {
    private LocalDateTime blockDatetime;
}
