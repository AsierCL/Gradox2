package com.example.gradox2.utils.mapper;

import java.util.stream.Collectors;

import com.example.gradox2.persistence.entities.User;
import com.example.gradox2.persistence.entities.enums.UserRole;
import com.example.gradox2.presentation.dto.auth.AuthResponse;
import com.example.gradox2.presentation.dto.auth.RegisterRequest;
import com.example.gradox2.presentation.dto.users.MyProfileResponse;
import com.example.gradox2.presentation.dto.users.PublicProfileResponse;

@Mapper(componentModel = "spring")
public interface DtoUserMapper {

    // ------------------------------
    // RegisterRequest -> User
    // ------------------------------
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", source = "passwordHash")
    @Mapping(target = "username", source = "dto.username")
    @Mapping(target = "email", source = "dto.email")
    @Mapping(target = "role", expression = "java(UserRole.USER)")
    @Mapping(target = "reputation", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "lastLogin", ignore = true)
    @Mapping(target = "badges", ignore = true)
    User toUserEntity(RegisterRequest dto, String passwordHash);

    // ------------------------------
    // User + token -> AuthResponse
    // ------------------------------
    @Mapping(target = "token", source = "token")
    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "role", expression = "java(user.getRole().name())")
    AuthResponse toAuthResponse(User user, String token);

    // ------------------------------
    // User -> MyProfileResponse
    // ------------------------------
    @Mapping(target = "role", expression = "java(user.getRole().name())")
    @Mapping(target = "badges", source = "user", qualifiedByName = "mapBadges")
    MyProfileResponse toProfileResponse(User user);

    // ------------------------------
    // User -> PublicProfileResponse
    // ------------------------------
    @Mapping(target = "role", expression = "java(user.getRole().name())")
    @Mapping(target = "badges", source = "user", qualifiedByName = "mapBadges")
    PublicProfileResponse toPublicProfileResponse(User user);

    // ------------------------------
    // Método auxiliar para mapear badges a nombres
    // ------------------------------
    @Named("mapBadges")
    default Set<String> mapBadges(User user) {
        return user.getBadges()
                   .stream()
                   .map(badge -> badge.getName())
                   .collect(Collectors.toSet());
    }
}
