package com.example.gradox2.utils.mapper;

import org.springframework.stereotype.Component;

import com.example.gradox2.persistence.entities.File;
import com.example.gradox2.presentation.dto.files.FileResponse;

@Component
public class FileMapper {

    public static final FileResponse toFileResponse(File file) {
        if (file == null) {
            return null;
        }

        return FileResponse.builder()
            .id(file.getId())
            .fileName(file.getTitle())
            .description(file.getDescription())
            .fileType(file.getType())
            .subject(file.getSubject() != null ? file.getSubject().getName() : null)
            .uploaderUsername(file.getUploader() != null ? file.getUploader().getUsername() : null)
            .score(file.calculateTotalScore())
            .build();
    }
}
