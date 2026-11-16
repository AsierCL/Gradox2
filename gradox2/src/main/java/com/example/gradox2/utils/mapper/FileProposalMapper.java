package com.example.gradox2.utils.mapper;

import com.example.gradox2.persistence.entities.Degree;
import com.example.gradox2.persistence.entities.File;
import com.example.gradox2.persistence.entities.FileProposal;
import com.example.gradox2.persistence.entities.Subject;
import com.example.gradox2.persistence.entities.TempFile;
import com.example.gradox2.persistence.entities.User;
import com.example.gradox2.persistence.entities.enums.FileVisibility;
import com.example.gradox2.persistence.entities.enums.ProposalStatus;
import com.example.gradox2.presentation.dto.fileProposal.FileProposalResponse;
import com.example.gradox2.utils.IdentityVisibility;

/**
 * Mapper de propuestas de archivo.
 *
 * <p>Una propuesta tiene tres fuentes posibles según su estado: el {@link File} publicado (aprobada), el
 * {@link TempFile} (subida pendiente) o el {@link File} objetivo (borrado). En los tres casos la asignatura
 * es el único dato que hace falta resolver, y de ella salen el curso y el grado: el resto del mapper no
 * vuelve a preguntar por la jerarquía.
 */
public final class FileProposalMapper {

    private FileProposalMapper() {
    }

    public static final FileProposalResponse toFileProposalResponse(FileProposal fileProposal, User viewer) {
        File publishedFile = fileProposal.getFile();
        TempFile tempFile = fileProposal.getTempFile();

        String title = null;
        String description = null;
        String type = null;
        FileVisibility visibilityLevel = FileVisibility.PUBLIC;
        Subject subject = null;

        if (fileProposal.getStatus() == ProposalStatus.APPROVED && publishedFile != null) {
            title = publishedFile.getTitle();
            description = publishedFile.getDescription();
            type = publishedFile.getType() != null ? publishedFile.getType().toString() : null;
            visibilityLevel = publishedFile.getVisibilityLevel();
            subject = publishedFile.getSubject();
        } else if (tempFile != null) {
            title = tempFile.getTitle();
            description = tempFile.getDescription();
            type = tempFile.getType() != null ? tempFile.getType().toString() : null;
            visibilityLevel = tempFile.getVisibilityLevel();
            subject = tempFile.getSubject();
        } else if (publishedFile != null) {
            title = publishedFile.getTitle();
            description = publishedFile.getDescription();
            type = publishedFile.getType() != null ? publishedFile.getType().toString() : null;
            visibilityLevel = publishedFile.getVisibilityLevel();
            subject = publishedFile.getSubject();
        }

        Degree degree = subject != null ? subject.getDegree() : null;

        return FileProposalResponse.builder()
                .id(fileProposal.getId())
                .title(title)
                .description(description)
                .proposer(IdentityVisibility.resolveDisplayUsername(
                        fileProposal.getProposer(), viewer, visibilityLevel))
                .visibilityLevel(visibilityLevel != null ? visibilityLevel.toString() : null)
                .subjectId(subject != null ? subject.getId() : null)
                .subjectCode(subject != null ? subject.getCode() : null)
                .subjectName(subject != null ? subject.getName() : null)
                .courseId(subject != null && subject.getCourse() != null ? subject.getCourse().getId() : null)
                .courseName(subject != null && subject.getCourse() != null ? subject.getCourse().getName() : null)
                .degreeId(degree != null ? degree.getId() : null)
                .degreeCode(degree != null ? degree.getCode() : null)
                .degreeName(degree != null ? degree.getName() : null)
                .type(type)
                .status(fileProposal.getStatus() != null ? fileProposal.getStatus().toString() : null)
                .build();
    }
}
