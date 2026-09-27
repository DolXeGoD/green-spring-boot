package com.green_computer.green_board.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReportProcessRequest {
    @NotNull
    private Boolean accepted;
}
