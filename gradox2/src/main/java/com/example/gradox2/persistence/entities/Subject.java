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
@Table(name = "subjects")
public class Subject {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    /** Créditos ECTS del plan oficial; admite decimales (hay asignaturas de 4,5). */
    private Double credits;

    /** «Primer semestre» / «Segundo semestre»; nulo en las asignaturas anuales. */
    @Column(length = 32)
    private String semester;

    /** Tipo de materia del plan oficial: formación básica, obligatorio, optativo, anual… */
    @Column(name = "subject_type", length = 64)
    private String subjectType;

    /**
     * Grado de la asignatura, derivado de su curso. No se persiste en {@code subjects} para no tener dos
     * fuentes de verdad que puedan divergir.
     */
    public Degree getDegree() {
        return course != null ? course.getDegree() : null;
    }

    @OneToMany(mappedBy = "subject")
    @Builder.Default
    private Set<File> resources = new HashSet<>();

}
