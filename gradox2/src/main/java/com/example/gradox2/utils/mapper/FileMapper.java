package com.example.gradox2.utils.mapper;

import com.example.gradox2.persistence.entities.Degree;
import com.example.gradox2.persistence.entities.File;
import com.example.gradox2.persistence.entities.User;
import com.example.gradox2.presentation.dto.files.FileResponse;
import com.example.gradox2.utils.IdentityVisibility;

public final class FileMapper {

    private FileMapper() {
    }

    /** El grado nunca se persiste en el archivo: se deriva de la asignatura y su curso. */
    private static Degree degreeOf(File file) {
        return file.getSubject() != null ? file.getSubject().getDegree() : null;
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
            .degreeId(degreeOf(file) != null ? degreeOf(file).getId() : null)
            .degreeCode(degreeOf(file) != null ? degreeOf(file).getCode() : null)
            .degreeName(degreeOf(file) != null ? degreeOf(file).getName() : null)
            .uploaderUsername(IdentityVisibility.resolveDisplayUsername(
                    file.getUploader(), viewer, file.getVisibilityLevel()))
            .visibilityLevel(file.getVisibilityLevel())
            .score(file.calculateTotalScore())
            .build();
    }
}
