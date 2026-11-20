package com.example.gradox2.persistence.entities;

import java.time.Instant;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
// WAVE1 S6: el índice único de la migración V15 también tiene que existir en el schema que Hibernate
// genera para los tests; si no, la garantía solo estaría probada en producción.
@Table(name = "scores", uniqueConstraints = @UniqueConstraint(
        name = "uk_scores_file_voter", columnNames = {"file_id", "voter_id"}))
public class Score {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "voter_id", nullable = false)
    private User user;

    private Double score;

    @ManyToOne
    @JoinColumn(name = "file_id", nullable = false)
    private File file;

    @Builder.Default
    private Instant scoredAt = Instant.now();
}
