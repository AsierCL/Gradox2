package com.example.gradox2.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.example.gradox2.persistence.entities.User;
import com.example.gradox2.service.exceptions.UnauthenticatedAccessException;

public final class GetAuthUser {
    public static User getAuthUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
               throw new UnauthenticatedAccessException("El usuario no está autenticado.");
        }
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof User uploader)) {
            throw new UnauthenticatedAccessException("El usuario no es una instancia de User.");
        }
        return uploader;
    }
}
