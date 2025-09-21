package com.example.gradox2.service.exceptions;

public class InvalidFileOperation extends RuntimeException {
    public InvalidFileOperation(String message) {
        super(message);
    }

    public InvalidFileOperation(String message, Throwable cause) {
        super(message, cause);
    }
}
