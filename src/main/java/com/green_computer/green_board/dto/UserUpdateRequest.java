package com.green_computer.green_board.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.Size;

@Getter
@Setter
@AllArgsConstructor
public class UserUpdateRequest {
    @Size(min = 1, max = 30)
    private String name;
}
