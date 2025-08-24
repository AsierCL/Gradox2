package com.example.gradox2.presentation.dto.files;

import org.springframework.web.multipart.MultipartFile;

import com.example.gradox2.persistence.entities.Subject;
import com.example.gradox2.persistence.entities.enums.FileType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class FileResponse {
    private String fileName;
    private FileType fileType;
    private Subject subject;
    private MultipartFile file;
}
