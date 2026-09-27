package com.green_computer.green_board.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReportCreateRequest {
    @NotBlank
    @Size(max = 1000)
    private String reason;
}
