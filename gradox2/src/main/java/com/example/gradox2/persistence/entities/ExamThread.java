package com.example.gradox2.persistence.entities;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "exam_threads")
public class ExamThread {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String examTitle;
    private Integer examYear;

    @ManyToOne
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @ManyToOne
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    private Instant createdAt = Instant.now();

    @OneToMany(mappedBy = "thread")
    private List<ExamResponse> responses = new ArrayList<>();
}