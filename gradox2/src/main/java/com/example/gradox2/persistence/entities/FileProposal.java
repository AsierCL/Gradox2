package com.example.gradox2.persistence.entities;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@DiscriminatorValue("UPLOAD")
public class FileProposal extends Proposal {

    // Archivo temporal asociado a la propuesta
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "temp_file_id", nullable = true)
    private TempFile tempFile;

    // Archivo definitivo, solo se llena cuando se aprueba
    @ManyToOne
    @JoinColumn(name = "file_id")
    private File file;
}
