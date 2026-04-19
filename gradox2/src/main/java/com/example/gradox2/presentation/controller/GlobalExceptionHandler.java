package com.example.gradox2.presentation.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.validation.BindException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.example.gradox2.presentation.dto.auth.ErrorDTO;
import com.example.gradox2.service.exceptions.UnauthenticatedAccessException;
import com.example.gradox2.service.exceptions.AlreadyExistsException;
import com.example.gradox2.service.exceptions.InternalServerErrorException;
import com.example.gradox2.service.exceptions.InvalidRoleOperationException;
import com.example.gradox2.service.exceptions.InvalidFileOperation;
import com.example.gradox2.service.exceptions.NotFoundException;
import com.example.gradox2.service.exceptions.ProposalClosedException;
import com.example.gradox2.service.exceptions.RateLimitExceededException;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;


@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final String VALIDATION_ERROR_CODE = "VALIDATION_ERROR";

    @ExceptionHandler(AlreadyExistsException.class)
    public ResponseEntity<ErrorDTO> handleAlreadyExists(AlreadyExistsException ex) {
        ErrorDTO error = ErrorDTO.builder()
                .errorMessage(ex.getMessage())
                .errorCode("ALREADY_EXIST_ERROR")
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
    public ResponseEntity<ErrorDTO> handleNotFound(NotFoundException ex) {
        ErrorDTO error = ErrorDTO.builder()
                .errorMessage(ex.getMessage())
                .errorCode("NOT_FOUND")
                .build();
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InvalidRoleOperationException.class)
    public ResponseEntity<ErrorDTO> handleInvalidRoleOperation(InvalidRoleOperationException ex) {
        ErrorDTO error = ErrorDTO.builder()
                .errorMessage(ex.getMessage())
                .errorCode("INVALID_ROLE_OPERATION")
                .build();
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ProposalClosedException.class)
    public ResponseEntity<ErrorDTO> handleProposalClosed(ProposalClosedException ex) {
        ErrorDTO error = ErrorDTO.builder()
                .errorMessage(ex.getMessage())
                .errorCode("PROPOSAL_CLOSED")
                .build();
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidFileOperation.class)
    public ResponseEntity<ErrorDTO> handleInvalidFileOperation(InvalidFileOperation ex) {
        ErrorDTO error = ErrorDTO.builder()
                .errorMessage(ex.getMessage())
                .errorCode("INVALID_FILE_OPERATION")
                .build();
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InternalServerErrorException.class)
    public ResponseEntity<ErrorDTO> handleInternalServerError(InternalServerErrorException ex) {
        ErrorDTO error = ErrorDTO.builder()
                .errorMessage("An unexpected error occurred. Please try again later.")
                .errorCode("INTERNAL_SERVER_ERROR")
                .build();
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ErrorDTO> handleRateLimitExceeded(RateLimitExceededException ex) {
        ErrorDTO error = ErrorDTO.builder()
                .errorMessage(ex.getMessage())
                .errorCode("RATE_LIMIT_EXCEEDED")
                .build();
        return new ResponseEntity<>(error, HttpStatus.TOO_MANY_REQUESTS);
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class,
            ConstraintViolationException.class, HttpMessageNotReadableException.class,
            MissingServletRequestParameterException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ErrorDTO> handleValidationErrors(Exception ex) {
        ErrorDTO error = ErrorDTO.builder()
                .errorMessage(validationMessage(ex))
                .errorCode(VALIDATION_ERROR_CODE)
                .build();
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    private String validationMessage(Exception ex) {
        if (ex instanceof MethodArgumentNotValidException methodArgumentNotValidException) {
            return methodArgumentNotValidException.getBindingResult().getFieldErrors().stream()
                    .findFirst()
                    .map(fieldError -> fieldError.getDefaultMessage())
                    .orElse("Invalid request data");
        }

        if (ex instanceof BindException bindException) {
            return bindException.getBindingResult().getFieldErrors().stream()
                    .findFirst()
                    .map(fieldError -> fieldError.getDefaultMessage())
                    .orElse("Invalid request data");
        }

        if (ex instanceof ConstraintViolationException constraintViolationException) {
            return constraintViolationException.getConstraintViolations().stream()
                    .map(ConstraintViolation::getMessage)
                    .findFirst()
                    .orElse("Invalid request data");
        }

        if (ex instanceof MissingServletRequestParameterException missingParameterException) {
            return "Missing required parameter: " + missingParameterException.getParameterName();
        }

        if (ex instanceof MethodArgumentTypeMismatchException typeMismatchException) {
            return "Invalid value for parameter: " + typeMismatchException.getName();
        }

        return "Invalid request data";
    }
}
