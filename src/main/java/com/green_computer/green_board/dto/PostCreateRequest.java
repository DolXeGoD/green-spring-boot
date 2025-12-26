package com.green_computer.green_board.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.bind.annotation.DeleteMapping;

@Getter
@Setter
@AllArgsConstructor
public class PostCreateRequest {
    @Size(min = 10)
    @NotBlank
    private String title;

    @Size(min = 10)
    @NotBlank
    private String content;
}
