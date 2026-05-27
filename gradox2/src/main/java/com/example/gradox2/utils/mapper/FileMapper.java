package com.example.gradox2.utils.mapper;

import org.springframework.stereotype.Component;

import com.example.gradox2.persistence.entities.File;
import com.example.gradox2.persistence.entities.User;
import com.example.gradox2.presentation.dto.files.FileResponse;
import com.example.gradox2.utils.IdentityVisibility;

@Component
public class FileMapper {

    public static final FileResponse toFileResponse(File file, User viewer) {
        if (file == null) {
            return null;
        }

        return FileResponse.builder()
            .id(file.getId())
            .fileName(file.getTitle())
            .description(file.getDescription())
            .fileType(file.getType())
            .subject(file.getSubject() != null ? file.getSubject().getName() : null)
            .uploaderUsername(IdentityVisibility.resolveDisplayUsername(file.getUploader(), viewer, file.isAnonymous()))
            .anonymous(file.isAnonymous())
            .score(file.calculateTotalScore())
            .build();
    }
}
