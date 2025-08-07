package com.example.gradox2.utils.mapper;

import com.example.gradox2.persistence.entities.User;
import com.example.gradox2.persistence.entities.enums.UserRole;
import com.example.gradox2.presentation.dto.AuthResponse;
import com.example.gradox2.presentation.dto.RegisterRequest;

public class DtoMapper {

    public static User toUserEntity(RegisterRequest dto, String passwordHash) {
        User user = new User();
        user.setUsername(dto.username);
        user.setEmail(dto.email);
        user.setPasswordHash(passwordHash);
        user.setRole(UserRole.USER);
        return user;
    }

    public static AuthResponse toAuthResponse(User user, String token) {
        AuthResponse response = new AuthResponse();
        response.token = token;
        response.username = user.getUsername();
        response.role = user.getRole().name();
        return response;
    }
}