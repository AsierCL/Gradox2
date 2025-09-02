package com.example.gradox2.utils.mapper;

import com.example.gradox2.persistence.entities.File;
import com.example.gradox2.persistence.entities.FileProposal;
import com.example.gradox2.persistence.entities.TempFile;
import com.example.gradox2.persistence.entities.enums.ProposalStatus;
import com.example.gradox2.presentation.dto.fileProposal.FileProposalResponse;

public class FileProposalMapper {

    public static final FileProposalResponse toFileProposalResponse(FileProposal fileProposal) {
        String title = null;
        String description = null;
        String type = null;
        String subjectName = null;
        String course = null;

        // Si la propuesta ya está aprobada, usar File
        if (fileProposal.getStatus() == ProposalStatus.APPROVED && fileProposal.getFile() != null) {
            File file = fileProposal.getFile();
            title = file.getTitle();
            description = file.getDescription();
            type = file.getType().toString();
            if (file.getSubject() != null) {
                subjectName = file.getSubject().getName();
                if (file.getSubject().getCourse() != null) {
                    course = file.getSubject().getCourse().getName();
                }
            }
        }
        // Si aún no está aprobada, usar TempFile
        else if (fileProposal.getTempFile() != null) {
            TempFile tempFile = fileProposal.getTempFile();
            title = tempFile.getTitle();
            description = tempFile.getDescription();
            type = tempFile.getType().toString();
            if (tempFile.getSubject() != null) {
                subjectName = tempFile.getSubject().getName();
                if (tempFile.getSubject().getCourse() != null) {
                    course = tempFile.getSubject().getCourse().getName();
                }
            }
        }

        return FileProposalResponse.builder()
                .id(fileProposal.getId())
                .title(title)
                .description(description)
                .proposer(fileProposal.getProposer() != null ? fileProposal.getProposer().getUsername() : null)
                .subjectName(subjectName)
                .course(course)
                .type(type)
                .status(fileProposal.getStatus() != null ? fileProposal.getStatus().toString() : null)
                .build();
    }

}
