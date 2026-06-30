package com.example.gradox2.persistence.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Getter
@Setter
@NoArgsConstructor
@Entity
@DiscriminatorValue("1")
public class FileProposal extends Proposal {

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "temp_file_id")
    private TempFile tempFile;

    @ManyToOne
    @JoinColumn(name = "file_id")
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private File file;
}
