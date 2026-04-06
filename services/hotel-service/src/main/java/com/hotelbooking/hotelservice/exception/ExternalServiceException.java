package com.hotelbooking.hotelservice.exception;

import lombok.Getter;

@Getter
public class ExternalServiceException extends RuntimeException {

    private final String code;

    public ExternalServiceException(String code, String message) {
        super(message);
        this.code = code;
    }
}

