package com.example.gradox2.presentation.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.example.gradox2.presentation.dto.auth.ErrorDTO;
import com.example.gradox2.service.exceptions.UnauthenticatedAccessException;
import com.example.gradox2.service.exceptions.AlreadyExistsException;
import com.example.gradox2.service.exceptions.InternalServerErrorException;
import com.example.gradox2.service.exceptions.InvalidRoleOperationException;
import com.example.gradox2.service.exceptions.NotFoundException;
import com.example.gradox2.service.exceptions.ProposalClosedException;


@RestControllerAdvice
public class GlobalExceptionHandler {

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

    @ExceptionHandler(InternalServerErrorException.class)
    public ResponseEntity<ErrorDTO> handleInternalServerError(InternalServerErrorException ex) {
        ErrorDTO error = ErrorDTO.builder()
                .errorMessage("An unexpected error occurred. Please try again later.")
                .errorCode("INTERNAL_SERVER_ERROR")
                .build();
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
