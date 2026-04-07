package com.example.gradox2.presentation.dto.voteConfig;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VoteConfigUpdateRequest {
    @NotNull(message = "Quorum is required")
    @Min(value = 1, message = "Quorum must be at least 1")
    @Max(value = 10000, message = "Quorum must be <= 10000")
    private Integer quorumRequired;

    @NotNull(message = "Approval threshold is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Approval threshold must be > 0")
    @DecimalMax(value = "1.0", inclusive = true, message = "Approval threshold must be <= 1")
    private Double approvalThreshold;
}
