package com.example.gradox2.service.interfaces;

import com.example.gradox2.persistence.entities.VoteConfig;

public interface IVoteConfigService {

    VoteConfig getConfig();

    VoteConfig updateConfig(Integer quorumRequired, Double approvalThreshold);
}
