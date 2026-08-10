package com.example.gradox2.utils.mapper;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.example.gradox2.persistence.entities.User;
import com.example.gradox2.presentation.dto.users.MyProfileResponse;
import com.example.gradox2.presentation.dto.users.PublicProfileResponse;
import com.example.gradox2.service.interfaces.FileUrlSigner;
import com.example.gradox2.utils.ContentDisposition;

public final class UserMapper {
    public static final UserMapper mapper = new UserMapper();

    private static final String PROFILE_PICTURE_DOWNLOAD_NAME = "profile";

    private UserMapper() {
    }

    public MyProfileResponse toMyProfileResponse(User user, FileUrlSigner signer) {
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
                profilePictureUrl(user.getProfilePictureKey(), signer),
                mapBadges(user));
    }

    public PublicProfileResponse toPublicProfileResponse(User user, FileUrlSigner signer) {
        if (user == null) {
            return null;
        }

        return new PublicProfileResponse(
            user.getUsername(),
            user.getRole().name(),
            user.getReputation(),
            profilePictureUrl(user.getProfilePictureKey(), signer),
            mapBadges(user));
    }

    private String profilePictureUrl(String key, FileUrlSigner signer) {
        if (key == null || key.isBlank()) {
            return null;
        }
        return signer.presignedGetUrl(key, ContentDisposition.inlineOf(PROFILE_PICTURE_DOWNLOAD_NAME));
    }

    private Set<String> mapBadges(User user) {
        Set<com.example.gradox2.persistence.entities.Badge> badges = user.getBadges();
        if (badges == null) {
            return Collections.emptySet();
        }
        return badges.stream()
                .filter(Objects::nonNull)
                .map(badge -> badge.getName())
                .collect(Collectors.toSet());
    }
}