package com.green_computer.green_board.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class PostResponse {
    private int id;
    private String title;
    private String content;
    private String author;
    private int hits;
    private int likeCount;
    private Boolean isLikedByMe;
    private LocalDateTime createdDatetime;
    private LocalDateTime updatedDatetime;
}
