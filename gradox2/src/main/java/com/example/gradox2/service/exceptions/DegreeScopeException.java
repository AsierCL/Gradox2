package com.example.gradox2.service.exceptions;

/**
 * Escritura sobre un recurso de otro grado. Se responde {@code 403 DEGREE_SCOPE_FORBIDDEN}
 * (GRADOS.md §5 y §8): la lectura es pública, la participación no.
 */
public class DegreeScopeException extends RuntimeException {

    public DegreeScopeException(String message) {
        super(message);
    }
}
