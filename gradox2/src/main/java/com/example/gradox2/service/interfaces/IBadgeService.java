package com.example.gradox2.service.interfaces;

import java.util.List;

import com.example.gradox2.presentation.dto.badges.BadgeResponse;

public interface IBadgeService {
    List<BadgeResponse> getAllBadges();
}