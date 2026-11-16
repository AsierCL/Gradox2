package com.example.gradox2.persistence.entities;

import java.util.HashSet;
import java.util.Set;


import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "courses")
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    /**
     * Grado al que pertenece el curso. El código de curso ({@code '1'..'4'}) solo es único dentro de un
     * grado: cada grado tiene su propio «Primer curso».
     */
    @ManyToOne
    @JoinColumn(name = "degree_id", nullable = false)
    private Degree degree;

    @OneToMany(mappedBy = "course")
    @Builder.Default
    private Set<Subject> subjects = new HashSet<>();
}
