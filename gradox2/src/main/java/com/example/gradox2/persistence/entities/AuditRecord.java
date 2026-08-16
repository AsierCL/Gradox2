package com.example.gradox2.persistence.entities;

import java.time.Instant;

import com.example.gradox2.persistence.entities.enums.ActionType;

import jakarta.persistence.*;
import lombok.*;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

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
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "actor_id", nullable = true)
    private User actor;

    @Enumerated(EnumType.STRING)
    private ActionType actionType;

    private String targetEntity;
    private Long targetId;
    private String details;
    private Instant timestamp = Instant.now();
}