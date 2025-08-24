package com.example.gradox2.persistence.entities;


import java.time.Instant;

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

    //@OneToMany(mappedBy = "resource")
    //private Set<Vote> votes = new HashSet<>();
}
