package com.example.gradox2.service.interfaces;

import java.util.List;

import org.springframework.data.domain.Page;
import com.example.gradox2.persistence.entities.TempFile;
import com.example.gradox2.presentation.dto.fileProposal.FileProposalResponse;
import com.example.gradox2.presentation.dto.fileProposal.UploadFileProposalRequest;

public interface IFileProposalService {
    FileProposalResponse uploadFileProposal(UploadFileProposalRequest dto);
    String deleteFileProposal(Long id);
    List<FileProposalResponse> getAllFileProposals();
    Page<FileProposalResponse> getFileProposalsPaged(int page, int size, String sortBy);
    FileProposalResponse getFileProposalById(Long id);
    TempFile downloadFileFromProposal(Long id);
}
