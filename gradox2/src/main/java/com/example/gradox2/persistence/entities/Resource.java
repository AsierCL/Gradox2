package com.example.gradox2.persistence.entities;


import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import com.example.gradox2.persistence.entities.enums.ResourceType;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "resources")
public class Resource {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;

    @Enumerated(EnumType.STRING)
    private ResourceType type;

    private String fileUrl;
    private Instant uploadDate = Instant.now();

    @ManyToOne
    @JoinColumn(name = "uploader_id", nullable = false)
    private User uploader;

    @ManyToOne
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @OneToMany(mappedBy = "resource")
    private Set<Vote> votes = new HashSet<>();
}