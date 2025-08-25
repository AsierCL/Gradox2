package com.example.gradox2.persistence.entities;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@DiscriminatorValue("UPLOAD")
public class UploadProposal extends Proposal {

    // Archivo temporal asociado a la propuesta
    @ManyToOne
    @JoinColumn(name = "temp_file_id", nullable = false)
    private TempFile tempFile;

    // Archivo definitivo, solo se llena cuando se aprueba
    @ManyToOne
    @JoinColumn(name = "file_id")
    private File file;
}
