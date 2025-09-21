package com.example.gradox2.service.exceptions;

public class InvalidRoleOperationException extends RuntimeException {

    public InvalidRoleOperationException(String message) {
        super(message);
    }

    public InvalidRoleOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
