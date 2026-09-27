package com.green_computer.green_board.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class CommentResponse {
    private int id;
    private int boardId;
    private String author;
    private String content;
    private LocalDateTime createdDatetime;
}
