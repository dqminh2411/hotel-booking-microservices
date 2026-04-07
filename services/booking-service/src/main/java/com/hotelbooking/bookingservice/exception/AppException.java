package com.hotelbooking.bookingservice.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class AppException extends RuntimeException {
    private final String code;
    private final HttpStatus status;

    public AppException(String code, String message, HttpStatus status) {
        super(message);
        this.code = code;
        this.status = status;
    }
}
