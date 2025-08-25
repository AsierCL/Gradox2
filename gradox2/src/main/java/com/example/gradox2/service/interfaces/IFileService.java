package com.example.gradox2.service.interfaces;

import java.util.List;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;

import com.example.gradox2.presentation.dto.fileProposal.UploadFileProposalRequest;
import com.example.gradox2.presentation.dto.files.FileResponse;

public interface IFileService {

    ResponseEntity<String> uploadFile(UploadFileProposalRequest dto);
    List<FileResponse> getAllFiles();
    FileResponse getFile(Long id);
    ResponseEntity<ByteArrayResource> downloadFile(Long id);
}
