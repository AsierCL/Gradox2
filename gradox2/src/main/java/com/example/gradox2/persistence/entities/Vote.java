package com.example.gradox2.persistence.entities;

import java.time.Instant;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "votes")
public class Vote {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "voter_id", nullable = false)
    private User voter;

    @ManyToOne
    @JoinColumn(name = "resource_id")
    private File resource;

    @ManyToOne
    @JoinColumn(name = "response_id")
    private ExamResponse response;

    @ManyToOne
    @JoinColumn(name = "proposal_id")
    private PromotionProposal proposal;

    private int value;
    private double weight;
    private Instant votedAt = Instant.now();

}