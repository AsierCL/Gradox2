package com.example.gradox2.service.interfaces;

import java.util.List;

import com.example.gradox2.persistence.entities.TempFile;
import com.example.gradox2.presentation.dto.fileProposal.FileProposalResponse;
import com.example.gradox2.presentation.dto.fileProposal.UploadFileProposalRequest;

public interface IFileProposalService {
    FileProposalResponse uploadFileProposal(UploadFileProposalRequest dto);
    String deleteFileProposal(Long id);
    List<FileProposalResponse> getAllFileProposals();
    FileProposalResponse getFileProposalById(Long id);
    TempFile downloadFileFromProposal(Long id);
}
