package com.example.gradox2.utils.mapper;

import com.example.gradox2.persistence.entities.FileProposal;
import com.example.gradox2.presentation.dto.fileProposal.FileProposalResponse;

public class FileProposalMapper {

    public static final FileProposalResponse toFileProposalResponse(FileProposal fileProposal) {
        FileProposalResponse fileProposalResponse = FileProposalResponse.builder()
                .id(fileProposal.getId())
                .title(fileProposal.getTempFile().getTitle())
                .description(fileProposal.getTempFile().getDescription())
                .proposer(fileProposal.getProposer().getUsername())
                .subjectName(fileProposal.getTempFile().getSubject().getName())
                .course(fileProposal.getTempFile().getSubject().getCourse().getName())
                .type(fileProposal.getTempFile().getType().toString())
                .status(fileProposal.getStatus().toString())
                .build();

        return fileProposalResponse;
    }
}
