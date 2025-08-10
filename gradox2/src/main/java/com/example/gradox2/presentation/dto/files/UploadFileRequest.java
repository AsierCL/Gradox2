package com.example.gradox2.presentation.dto.files;

import org.springframework.web.multipart.MultipartFile;

import com.example.gradox2.persistence.entities.enums.ResourceType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class UploadFileRequest {
    private String title;
    private String description;
    private ResourceType type;
    private Long subjectId;
    private MultipartFile file;
}