package com.backend.chess_backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class IllegalActivity extends RuntimeException {
    public IllegalActivity(String message) {
        super(message);
    }
}
