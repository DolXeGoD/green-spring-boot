package com.green_computer.green_board.global;

import com.green_computer.green_board.dto.ApiResponse;
import com.green_computer.green_board.exceptions.AuthenticationFailureException;
import com.green_computer.green_board.exceptions.AuthorizationFailureException;
import com.green_computer.green_board.exceptions.InvalidStateException;
import com.green_computer.green_board.exceptions.ResourceNotFoundException;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@Hidden // Swagger 가 이 파일은 무시하도록 처리함
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    // 코드 전역에서 떨어지는 Exception 을 처리하는 역할
    // ResourceNotFoundException
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(ResourceNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.fail(e.getMessage()));
    }

    // AuthorizationFailureException
    @ExceptionHandler({
            AuthorizationFailureException.class, // 우리 커스텀 권한 거절 exception
            AuthorizationDeniedException.class // 스프링 시큐리티 권한 거절 exception
    })
    public ResponseEntity<ApiResponse<?>> handleUnauthorized(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.fail(e.getMessage()));
    }
    // AuthenticationFailureException
    @ExceptionHandler(AuthenticationFailureException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnauthenticated(AuthenticationFailureException e) {
        System.out.println("403 실행됨");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.fail(e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationError(MethodArgumentNotValidException e) {
        String resultMessage = "";
        List<FieldError> errors = e.getBindingResult().getFieldErrors();
        for (FieldError error : errors) {
            resultMessage = resultMessage + error.getField() + "은(는)" + error.getDefaultMessage() + "\n";
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail(resultMessage));
    }

    @ExceptionHandler(InvalidStateException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidState(InvalidStateException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail(e.getMessage()));
    }

    // Exception (그 외 처리하지 않은 모든 예외들)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        log.error(e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.fail(e.getMessage()));
    }
}
