package com.example.gradox2.service.implementation;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.gradox2.persistence.entities.VoteConfig;
import com.example.gradox2.persistence.repository.VoteConfigRepository;
import com.example.gradox2.service.interfaces.IVoteConfigService;

@Service
public class VoteConfigServiceImpl implements IVoteConfigService {

    private static final int DEFAULT_QUORUM_REQUIRED = 5;
    private static final double DEFAULT_APPROVAL_THRESHOLD = 0.6;

    private final VoteConfigRepository voteConfigRepository;

    public VoteConfigServiceImpl(VoteConfigRepository voteConfigRepository) {
        this.voteConfigRepository = voteConfigRepository;
    }

        @Transactional
        public VoteConfig getConfig() {
        return voteConfigRepository.findAll().stream().findFirst()
            .orElseGet(() -> voteConfigRepository.save(
                VoteConfig.builder()
                    .quorumRequired(DEFAULT_QUORUM_REQUIRED)
                    .approvalThreshold(DEFAULT_APPROVAL_THRESHOLD)
                    .build()
            ));
    }

    @Transactional
    public VoteConfig updateConfig(Integer quorumRequired, Double approvalThreshold) {
            VoteConfig config = getConfig();
        config.setQuorumRequired(quorumRequired);
        config.setApprovalThreshold(approvalThreshold);
        return voteConfigRepository.save(config);
    }
}
