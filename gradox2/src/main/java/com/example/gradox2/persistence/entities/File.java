package com.example.gradox2.persistence.entities;


import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.example.gradox2.persistence.entities.enums.FileType;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "files")
public class File {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;

    @Enumerated(EnumType.STRING)
    private FileType type;

    @Lob
    @Column(name = "file_data", nullable = false)
    private byte[] fileData;
    private String fileHash;

    @Builder.Default
    private Instant uploadAt = Instant.now();

    @ManyToOne
    @JoinColumn(name = "uploader_id", nullable = false)
    private User uploader;

    @ManyToOne
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    private Double score;

    @OneToMany(mappedBy = "file", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Score> scores = new ArrayList<>();

    // Método helper para mantener la consistencia bidireccional
    public void addScore(Score score) {
        scores.add(score);
        score.setFile(this);
        // Actualizar el score total
        this.score = calculateTotalScore();
    }

    public void removeScore(Score score) {
        scores.remove(score);
        score.setFile(null);
        // Actualizar el score total
        this.score = calculateTotalScore();
    }

    // El score total ahora se puede calcular dinámicamente
    public Double calculateTotalScore() {
        return scores.stream()
                .mapToDouble(Score::getScore)
                .average()
                .orElse(0.0);
    }

}
