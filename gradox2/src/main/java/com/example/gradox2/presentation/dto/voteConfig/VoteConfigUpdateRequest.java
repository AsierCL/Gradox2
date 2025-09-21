package com.example.gradox2.presentation.dto.voteConfig;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VoteConfigUpdateRequest {
    private Integer quorumRequired;
    private Double approvalThreshold;
}
