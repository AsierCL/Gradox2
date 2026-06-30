package com.example.gradox2.persistence.entities;

import java.time.Instant;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "votes", uniqueConstraints = {
    @UniqueConstraint(name = "uk_votes_voter_proposal", columnNames = {"voter_id", "proposal_id"})
})
public class Vote {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "voter_id", nullable = false)
    private User voter;

    @ManyToOne
    @JoinColumn(name = "proposal_id", nullable = false)
    private Proposal proposal;

    @Builder.Default
    private Double weight = 1.0;
    private Boolean inFavor;

    @Builder.Default
    private Instant votedAt = Instant.now();

}
