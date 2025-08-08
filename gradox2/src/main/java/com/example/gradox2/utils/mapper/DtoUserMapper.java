package com.example.gradox2.utils.mapper;

import java.util.stream.Collectors;

import com.example.gradox2.persistence.entities.User;
import com.example.gradox2.persistence.entities.enums.UserRole;
import com.example.gradox2.presentation.dto.AuthResponse;
import com.example.gradox2.presentation.dto.ProfileResponse;
import com.example.gradox2.presentation.dto.RegisterRequest;

public class DtoUserMapper {

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

    public static ProfileResponse toProfileResponse(User user) {
        ProfileResponse dto = new ProfileResponse();
        dto.id = user.getId();
        dto.username = user.getUsername();
        dto.email = user.getEmail();
        dto.role = user.getRole().name();
        dto.reputation = user.getReputation();
        dto.createdAt = user.getCreatedAt();
        dto.lastLogin = user.getLastLogin();

        // Convertimos las badges a un Set de Strings con los nombres
        dto.badges = user.getBadges()
                        .stream()
                        .map(badge -> badge.getName()) // asegúrate de que Badge tiene getName()
                        .collect(Collectors.toSet());

        return dto;
    }
}