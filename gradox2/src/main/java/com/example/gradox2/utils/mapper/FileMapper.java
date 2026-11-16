package com.example.gradox2.utils.mapper;

import com.example.gradox2.persistence.entities.File;
import com.example.gradox2.persistence.entities.User;
import com.example.gradox2.presentation.dto.files.FileResponse;
import com.example.gradox2.utils.IdentityVisibility;

public final class FileMapper {

    private FileMapper() {
    }

    private static Long degreeId(File file) {
        return file.getSubject() != null && file.getSubject().getDegree() != null
                ? file.getSubject().getDegree().getId() : null;
    }

    private static String degreeCode(File file) {
        return file.getSubject() != null && file.getSubject().getDegree() != null
                ? file.getSubject().getDegree().getCode() : null;
    }

    private static String degreeName(File file) {
        return file.getSubject() != null && file.getSubject().getDegree() != null
                ? file.getSubject().getDegree().getName() : null;
    }

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
            .subjectId(file.getSubject() != null ? file.getSubject().getId() : null)
            .subjectCode(file.getSubject() != null ? file.getSubject().getCode() : null)
            .degreeId(degreeId(file))
            .degreeCode(degreeCode(file))
            .degreeName(degreeName(file))
            .uploaderUsername(IdentityVisibility.resolveDisplayUsername(
                    file.getUploader(), viewer, file.getVisibilityLevel()))
            .visibilityLevel(file.getVisibilityLevel())
            .score(file.calculateTotalScore())
            .build();
    }
}
