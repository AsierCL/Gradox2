package com.example.gradox2.service.interfaces;

import java.util.List;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;

import com.example.gradox2.persistence.entities.FileProposal;
import com.example.gradox2.presentation.dto.files.UploadFileRequest;

public interface IFileProposalService {
    ResponseEntity<String> uploadFileProposal(UploadFileRequest dto);
    ResponseEntity<String> deleteFileProposal(Long id);
    List<FileProposal> getAllFileProposals();
    FileProposal getFileProposalById(Long id);
    ResponseEntity<ByteArrayResource> downloadFileFromProposal(Long id);
}
