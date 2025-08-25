package com.example.gradox2.persistence.entities;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import com.example.gradox2.persistence.entities.enums.ProposalStatus;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "proposals")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "proposal_type", discriminatorType = DiscriminatorType.STRING)
public abstract class Proposal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "proposer_id", nullable = false)
    private User proposer;

    @OneToMany(mappedBy = "proposal", cascade = CascadeType.ALL)
    private Set<Vote> votes = new HashSet<>();

    private int quorumRequired;
    private double approvalThreshold;
    private Instant createdAt = Instant.now();
    private Instant endsAt = createdAt.plusSeconds(7 * 24 * 60 * 60); // Por defecto, 7 días

    private Instant closedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProposalStatus status = ProposalStatus.PENDING;
}
