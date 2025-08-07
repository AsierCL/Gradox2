package com.example.gradox2.persistence.entities;

import java.time.Instant;

import com.example.gradox2.persistence.entities.enums.ActionType;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "audit_records")
public class AuditRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "actor_id", nullable = false)
    private User actor;

    @Enumerated(EnumType.STRING)
    private ActionType actionType;

    private String targetEntity;
    private Long targetId;
    private String details;
    private Instant timestamp = Instant.now();
}