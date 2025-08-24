package com.example.gradox2.presentation.dto.files;

import org.springframework.web.multipart.MultipartFile;

import com.example.gradox2.persistence.entities.Subject;
import com.example.gradox2.persistence.entities.enums.FileType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class FileResponse {
    private Long id;
    private String fileName;
    private String description;
    private FileType fileType;
    private String subject;
    private String uploaderUsername;
    private MultipartFile file;
}
