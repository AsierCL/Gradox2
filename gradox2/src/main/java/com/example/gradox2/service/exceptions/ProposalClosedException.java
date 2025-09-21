package com.example.gradox2.service.exceptions;

public class ProposalClosedException extends RuntimeException {

    public ProposalClosedException(String message) {
        super(message);
    }

    public ProposalClosedException(String message, Throwable cause) {
        super(message, cause);
    }
}
