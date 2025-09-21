package com.example.gradox2.service.implementation;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.gradox2.persistence.entities.VoteConfig;
import com.example.gradox2.persistence.repository.VoteConfigRepository;
import com.example.gradox2.service.exceptions.InternalServerErrorException;
import com.example.gradox2.service.interfaces.IVoteConfigService;

@Service
public class VoteConfigServiceImpl implements IVoteConfigService {

    private final VoteConfigRepository voteConfigRepository;

    public VoteConfigServiceImpl(VoteConfigRepository voteConfigRepository) {
        this.voteConfigRepository = voteConfigRepository;
    }

    public VoteConfig getConfig() {
        return voteConfigRepository.findById(1L) // Registro fijo
                .orElseThrow(() -> new InternalServerErrorException("Vote configuration not found"));
    }

    @Transactional
    public VoteConfig updateConfig(Integer quorumRequired, Double approvalThreshold) {
            VoteConfig config = getConfig();
        config.setQuorumRequired(quorumRequired);
        config.setApprovalThreshold(approvalThreshold);
        return voteConfigRepository.save(config);
    }
}
