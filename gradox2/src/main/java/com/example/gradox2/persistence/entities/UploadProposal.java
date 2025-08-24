package com.example.gradox2.persistence.entities;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@DiscriminatorValue("UPLOAD")
public class UploadProposal extends Proposal {
    @ManyToOne
    @JoinColumn(name = "file_id", nullable = false)
    private File file;
}
