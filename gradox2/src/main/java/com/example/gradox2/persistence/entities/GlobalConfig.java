package com.example.gradox2.persistence.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "vote_config")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GlobalConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer quorumRequired;

    @Column(nullable = false)
    private Double approvalThreshold;

    @Column(nullable = false)
    private Integer maxPendingUploads;

    @Column(name = "master_vote_weight", nullable = false)
    @Builder.Default
    private Double masterVoteWeight = 2.0;

    @Column(name = "user_vote_weight", nullable = false)
    @Builder.Default
    private Double userVoteWeight = 1.0;
}
