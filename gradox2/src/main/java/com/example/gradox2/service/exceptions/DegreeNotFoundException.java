package com.example.gradox2.service.exceptions;

/**
 * El grado indicado no existe. Se responde {@code 400 DEGREE_NOT_FOUND} (GRADOS.md §8):
 * es un dato inválido del cliente, no un recurso ausente, por eso no es un {@code 404}.
 */
public class DegreeNotFoundException extends RuntimeException {

    public DegreeNotFoundException(String message) {
        super(message);
    }
}
