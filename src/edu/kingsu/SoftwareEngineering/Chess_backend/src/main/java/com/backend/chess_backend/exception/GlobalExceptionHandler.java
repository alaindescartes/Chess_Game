package com.backend.chess_backend.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import java.time.Instant;
import java.util.UUID;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(IllegalActivity.class)
    public ResponseEntity<ErrorResponse> handleIllegalActivityException(IllegalActivity ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.UNPROCESSABLE_ENTITY;
        String errorId = UUID.randomUUID().toString();

        log.error("errorId={} path={} status={} reason={} message={}",
                errorId, request.getRequestURI(), status.value(), status.getReasonPhrase(), ex.getMessage(), ex);

        ErrorResponse body = new ErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI(),
                errorId
        );

        return ResponseEntity.status(status).body(body);
    }

    public static record ErrorResponse(
            Instant timestamp,
            int status,
            String error,
            String message,
            String path,
            String errorId
    ) {}
}
