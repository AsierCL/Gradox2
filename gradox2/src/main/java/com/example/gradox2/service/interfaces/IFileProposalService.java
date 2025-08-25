package com.example.gradox2.service.interfaces;

import java.util.List;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;

import com.example.gradox2.presentation.dto.fileProposal.FileProposalResponse;
import com.example.gradox2.presentation.dto.fileProposal.UploadFileProposalRequest;

public interface IFileProposalService {
    ResponseEntity<String> uploadFileProposal(UploadFileProposalRequest dto);
    ResponseEntity<String> deleteFileProposal(Long id);
    List<FileProposalResponse> getAllFileProposals();
    FileProposalResponse getFileProposalById(Long id);
    ResponseEntity<ByteArrayResource> downloadFileFromProposal(Long id);
}
