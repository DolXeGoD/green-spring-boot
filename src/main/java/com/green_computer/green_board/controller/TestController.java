package com.green_computer.green_board.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController // 이 클래스는 사용자의 요청만 처리하는 Controller(안내 직원) 역할이다.
@RequestMapping("/api/test") // 얘는 URL이 /api/test/어쩌구 로 들어온 요청만 처리한다.
public class TestController {

    @GetMapping("/hello")
    // HTTP Method는 GET을 쓰겠다.
    // URL이 /hello 로 끝나는 경우 아래 메서드를 실행하겠다.
    // -> /api/test/hello
    public String test() {
        return "hello spring!";
    }
}
