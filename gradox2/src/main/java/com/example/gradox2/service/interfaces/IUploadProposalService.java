package com.example.gradox2.service.interfaces;

import java.util.List;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;

import com.example.gradox2.persistence.entities.UploadProposal;
import com.example.gradox2.presentation.dto.files.UploadFileRequest;

public interface IUploadProposalService {
    ResponseEntity<String> uploadProposal(UploadFileRequest dto);
    ResponseEntity<String> deleteProposal(Long id);
    List<UploadProposal> getAllProposals();
    UploadProposal getProposalById(Long id);
    ResponseEntity<ByteArrayResource> downloadProposalFile(Long id);
}
