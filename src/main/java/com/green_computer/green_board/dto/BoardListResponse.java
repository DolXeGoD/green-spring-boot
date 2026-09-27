package com.green_computer.green_board.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@AllArgsConstructor
public class BoardListResponse {
    private List<PostResponse> notices;
    private Page<PostResponse> posts;
}
