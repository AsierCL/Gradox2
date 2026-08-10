package com.example.gradox2.utils.mapper;

import com.example.gradox2.persistence.entities.Badge;
import com.example.gradox2.presentation.dto.badges.BadgeResponse;
import com.example.gradox2.service.interfaces.FileUrlSigner;
import com.example.gradox2.utils.ContentDisposition;

public final class BadgeMapper {

    private BadgeMapper() {
    }

    public static BadgeResponse toResponse(Badge badge, FileUrlSigner signer) {
        String iconUrl = null;
        if (badge.getIconKey() != null && !badge.getIconKey().isBlank()) {
            iconUrl = signer.presignedGetUrl(badge.getIconKey(),
                    ContentDisposition.inlineOf(badge.getName() + ".png"));
        }
        return new BadgeResponse(badge.getId(), badge.getName(), badge.getDescription(), iconUrl);
    }
}