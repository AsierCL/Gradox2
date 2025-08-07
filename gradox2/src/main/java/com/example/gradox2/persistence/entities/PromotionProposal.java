package com.example.gradox2.persistence.entities;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import com.example.gradox2.persistence.entities.enums.ProposalStatus;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "promotion_proposals")
public class PromotionProposal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "candidate_id", nullable = false)
    private User candidate;

    @ManyToOne
    @JoinColumn(name = "proposer_id", nullable = false)
    private User proposer;

    @OneToMany(mappedBy = "proposal")
    private Set<Vote> votes = new HashSet<>();

    private int quorumRequired;
    private double approvalThreshold;
    private Instant createdAt = Instant.now();
    private Instant endsAt;

    @Enumerated(EnumType.STRING)
    private ProposalStatus status;
}