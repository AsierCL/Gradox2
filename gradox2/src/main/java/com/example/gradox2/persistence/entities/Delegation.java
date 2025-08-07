package com.example.gradox2.persistence.entities;

import java.time.Instant;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "delegations")
public class Delegation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "delegator_id", nullable = false)
    private User delegator;

    @ManyToOne
    @JoinColumn(name = "delegatee_id", nullable = false)
    private User delegatee;

    @ManyToOne
    @JoinColumn(name = "subject_id")
    private Subject subject;

    private Instant delegatedAt = Instant.now();
    private boolean active = true;
}