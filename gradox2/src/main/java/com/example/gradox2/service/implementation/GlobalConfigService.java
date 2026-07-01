package com.example.gradox2.service.implementation;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.gradox2.persistence.entities.GlobalConfig;
import com.example.gradox2.persistence.repository.VoteConfigRepository;
import com.example.gradox2.service.interfaces.IGlobalConfigService;

@Service
public class GlobalConfigService implements IGlobalConfigService {

    private static final int DEFAULT_QUORUM_REQUIRED = 5;
    private static final double DEFAULT_APPROVAL_THRESHOLD = 0.6;
    private static final int DEFAULT_MAX_PENDING_UPLOADS = 3;

    private final VoteConfigRepository voteConfigRepository;
    private volatile GlobalConfig cachedConfig;

    public GlobalConfigService(VoteConfigRepository voteConfigRepository) {
        this.voteConfigRepository = voteConfigRepository;
    }

    @PostConstruct
    public void preloadConfig() {
        reloadConfig();
    }

    @Override
    @Transactional
    public GlobalConfig getConfig() {
        if (cachedConfig == null) {
            reloadConfig();
        }

        return cachedConfig;
    }

    @Override
    public synchronized void reloadConfig() {
        cachedConfig = loadOrCreateConfig();
    }

    @Override
    @Transactional
    public GlobalConfig updateConfig(Integer quorumRequired, Double approvalThreshold, Integer maxPendingUploads) {
        GlobalConfig config = getConfig();
        if (quorumRequired != null) {
            config.setQuorumRequired(quorumRequired);
        }
        if (approvalThreshold != null) {
            config.setApprovalThreshold(approvalThreshold);
        }
        if (maxPendingUploads != null) {
            config.setMaxPendingUploads(maxPendingUploads);
        }

        cachedConfig = voteConfigRepository.save(config);
        return cachedConfig;
    }

    private synchronized GlobalConfig loadOrCreateConfig() {
        return voteConfigRepository.findFirstByOrderByIdAsc()
            .orElseGet(() -> voteConfigRepository.save(
                GlobalConfig.builder()
                    .quorumRequired(DEFAULT_QUORUM_REQUIRED)
                    .approvalThreshold(DEFAULT_APPROVAL_THRESHOLD)
                    .maxPendingUploads(DEFAULT_MAX_PENDING_UPLOADS)
                    .build()
            ));
    }
}