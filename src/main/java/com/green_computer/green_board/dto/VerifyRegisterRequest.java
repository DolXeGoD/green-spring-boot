package com.green_computer.green_board.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerifyRegisterRequest {
    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String code;
}
