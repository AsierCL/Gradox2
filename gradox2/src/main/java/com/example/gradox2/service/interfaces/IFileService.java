package com.example.gradox2.service.interfaces;

import java.util.List;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;

import com.example.gradox2.presentation.dto.files.FileResponse;
import com.example.gradox2.presentation.dto.files.UploadFileRequest;

public interface IFileService {

    ResponseEntity uploadFile(UploadFileRequest dto);
    List<FileResponse> getAllFiles();
    FileResponse getFile(Long id);
    public ResponseEntity<ByteArrayResource> downloadFile(Long id);
}
