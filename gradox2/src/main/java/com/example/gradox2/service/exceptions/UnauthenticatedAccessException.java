package com.example.gradox2.service.exceptions;

public class UnauthenticatedAccessException extends RuntimeException {

    public UnauthenticatedAccessException(String message) {
        super(message);
    }

    public UnauthenticatedAccessException(String message, Throwable cause) {
        super(message, cause);
    }

}
