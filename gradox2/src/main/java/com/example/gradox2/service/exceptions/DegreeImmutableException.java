package com.example.gradox2.service.exceptions;

/**
 * Intento de cambiar el grado por una vía que no es el override administrativo (D2).
 *
 * <p>El grado se elige una vez, en el registro; ningún endpoint de usuario permite modificarlo. Se responde
 * {@code 403 DEGREE_IMMUTABLE} en lugar de ignorar el campo, porque ignorarlo haría creer al cliente que el
 * cambio se aplicó.
 */
public class DegreeImmutableException extends RuntimeException {

    public DegreeImmutableException(String message) {
        super(message);
    }
}
