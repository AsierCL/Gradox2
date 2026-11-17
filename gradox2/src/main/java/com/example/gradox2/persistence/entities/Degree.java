package com.example.gradox2.persistence.entities;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.*;
import lombok.*;

/**
 * Grado universitario. Es el nivel superior de la jerarquía del dominio:
 * {@code Degree → Course → Subject → File}.
 *
 * <p>Cada usuario pertenece a un grado de forma permanente y solo puede <b>escribir</b> dentro de él;
 * la lectura es pública para todos. El grado de un recurso nunca se guarda de forma redundante: se deriva
 * de su curso ({@link Course#getDegree()}), que es la única fuente de verdad.
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "degrees")
public class Degree {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Código oficial de la USC: {@code G4012}, {@code G4022}, {@code G4121}. */
    @Column(nullable = false, unique = true, length = 16)
    private String code;

    @Column(nullable = false)
    private String name;

    @OneToMany(mappedBy = "degree")
    @Builder.Default
    private Set<Course> courses = new HashSet<>();
}
