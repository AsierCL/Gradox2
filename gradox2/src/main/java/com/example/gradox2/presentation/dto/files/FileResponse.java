package com.example.gradox2.presentation.dto.files;

import com.example.gradox2.persistence.entities.Subject;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class FileResponse {
    private String fileName;
    private String fileType;
    private Subject subject;
    private byte[] data;
}
