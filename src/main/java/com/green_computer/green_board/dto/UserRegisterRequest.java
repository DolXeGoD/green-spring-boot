package com.green_computer.green_board.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UserRegisterRequest {
    @NotBlank
    private String username;

    @NotBlank
    private String name;

    @NotBlank
    @Min(value = 6)
    private String password;
}
