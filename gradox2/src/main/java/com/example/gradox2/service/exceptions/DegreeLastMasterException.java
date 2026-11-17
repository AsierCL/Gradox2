package com.example.gradox2.service.exceptions;

/**
 * La operación dejaría a un grado sin ningún master activo. Se responde {@code 409 DEGREE_LAST_MASTER}
 * (GRADOS.md §8): es un conflicto con el estado de la comunidad, no un error de validación.
 */
public class DegreeLastMasterException extends RuntimeException {

    public DegreeLastMasterException(String message) {
        super(message);
    }
}
