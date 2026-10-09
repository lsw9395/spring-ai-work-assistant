package com.portfolio.workassistant.web;

import com.portfolio.workassistant.ai.AiException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestControllerAdvice
public class ApiExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);
    public record ApiError(String code, String message, String requestId) {}

    @ExceptionHandler(AiException.class)
    ResponseEntity<ApiError> ai(AiException e) {
        return ResponseEntity.status(e.status()).body(new ApiError(e.code(), e.getMessage(), UUID.randomUUID().toString()));
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, HttpMessageNotReadableException.class})
    ResponseEntity<ApiError> invalid(Exception e) {
        return ResponseEntity.badRequest().body(new ApiError("INVALID_REQUEST",
                "입력 형식을 확인해 주세요. 메시지는 1~2,000자이며, 대화 이력은 최대 10개입니다.", UUID.randomUUID().toString()));
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiError> unexpected(Exception e) {
        String id = UUID.randomUUID().toString();
        log.error("Unexpected failure requestId={} type={}", id, e.getClass().getSimpleName());
        return ResponseEntity.internalServerError().body(new ApiError("INTERNAL_ERROR",
                "요청 처리 중 문제가 발생했습니다. 다시 시도해 주세요.", id));
    }
}
