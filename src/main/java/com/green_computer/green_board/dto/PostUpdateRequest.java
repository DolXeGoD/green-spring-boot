package com.green_computer.green_board.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.validation.constraints.Size;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PostUpdateRequest {
    @Size(min = 10, max = 50)
    private String title;
    @Size(min = 10)
    private String content;

}
