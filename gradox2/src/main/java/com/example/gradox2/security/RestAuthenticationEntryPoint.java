package com.example.gradox2.security;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.example.gradox2.presentation.dto.auth.ErrorDTO;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Responde 401 con el mismo envelope de error que el resto de la API cuando la
 * petición llega sin token o con un token inválido/expirado.
 *
 * <p>Sin este handler Spring Security usa {@code Http403ForbiddenEntryPoint} y
 * devuelve 403 sin body, así que el cliente no puede distinguir "no estoy
 * logueado" de "no tengo permiso".
 */
@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public RestAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException authException) throws IOException {
        ErrorDTO error = ErrorDTO.builder()
                .errorMessage("Authentication required")
                .errorCode("UNAUTHENTICATED_ACCESS")
                .build();

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getOutputStream(), error);
    }
}
