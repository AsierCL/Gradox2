package com.example.gradox2.presentation.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.example.gradox2.presentation.dto.auth.ErrorDTO;
import com.example.gradox2.service.exceptions.UnauthenticatedAccessException;
import com.example.gradox2.service.exceptions.AlreadyExistsException;
import com.example.gradox2.service.exceptions.NotFoundException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AlreadyExistsException.class)
    public ResponseEntity<ErrorDTO> handleUserAlreadyExists(AlreadyExistsException ex) {
        ErrorDTO error = ErrorDTO.builder()
                .errorMessage(ex.getMessage())
                .errorCode("USER_ALREADY_EXISTS")
                .build();
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(UnauthenticatedAccessException.class)
    public ResponseEntity<ErrorDTO> handleUnauthenticatedAccess(UnauthenticatedAccessException ex) {
        ErrorDTO error = ErrorDTO.builder()
                .errorMessage(ex.getMessage())
                .errorCode("UNAUTHENTICATED_ACCESS")
                .build();
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorDTO> handleUserNotFound(NotFoundException ex) {
        ErrorDTO error = ErrorDTO.builder()
                .errorMessage(ex.getMessage())
                .errorCode("USER_NOT_FOUND")
                .build();
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }
}
