package com.example.gradox2.service.exceptions;

/**
 * El registro (o una escritura de ámbito) necesita un grado y no lo tiene.
 * Se responde {@code 400 DEGREE_REQUIRED} (GRADOS.md §8).
 */
public class DegreeRequiredException extends RuntimeException {

    public DegreeRequiredException(String message) {
        super(message);
    }
}
