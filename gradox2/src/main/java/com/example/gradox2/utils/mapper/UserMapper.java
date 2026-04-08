package com.example.gradox2.utils.mapper;

import java.util.Set;
import java.util.stream.Collectors;

import com.example.gradox2.persistence.entities.User;
import com.example.gradox2.presentation.dto.users.MyProfileResponse;
import com.example.gradox2.presentation.dto.users.PublicProfileResponse;

public final class UserMapper {
    public static final UserMapper mapper = new UserMapper();

    private UserMapper() {
    }

    public MyProfileResponse toMyProfileResponse(User user) {
        if (user == null) {
            return null;
        }

        return new MyProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole().name(),
                user.getReputation(),
                user.getCreatedAt(),
                user.getLastLogin(),
                mapBadges(user));
    }

    public PublicProfileResponse toPublicProfileResponse(User user) {
        if (user == null) {
            return null;
        }

        return new PublicProfileResponse(
            user.getUsername(),
            user.getRole().name(),
            user.getReputation(),
            mapBadges(user));
    }

    private Set<String> mapBadges(User user) {
        return user.getBadges()
                .stream()
                .map(badge -> badge.getName())
                .collect(Collectors.toSet());
    }
}
