package com.example.gradox2.service.implementation;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.gradox2.persistence.repository.BadgeRepository;
import com.example.gradox2.presentation.dto.badges.BadgeResponse;
import com.example.gradox2.service.interfaces.FileUrlSigner;
import com.example.gradox2.service.interfaces.IBadgeService;
import com.example.gradox2.utils.mapper.BadgeMapper;

@Service
public class BadgeServiceImpl implements IBadgeService {
    private final BadgeRepository badgeRepository;
    private final FileUrlSigner fileUrlSigner;

    public BadgeServiceImpl(BadgeRepository badgeRepository, FileUrlSigner fileUrlSigner) {
        this.badgeRepository = badgeRepository;
        this.fileUrlSigner = fileUrlSigner;
    }

    @Transactional(readOnly = true)
    public List<BadgeResponse> getAllBadges() {
        return badgeRepository.findAllByOrderByNameAsc().stream()
                .map(badge -> BadgeMapper.toResponse(badge, fileUrlSigner))
                .toList();
    }
}