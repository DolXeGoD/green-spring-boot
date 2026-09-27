package com.green_computer.green_board.dto;

import com.green_computer.green_board.enums.ReportStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ReportResponse {
    private int id;
    private Integer boardId;
    private Integer commentId;
    private String reporter;
    private String reason;
    private ReportStatus status;
    private LocalDateTime createdDatetime;
}
