package com.example.gradox2.service.interfaces;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.example.gradox2.presentation.dto.files.FileResponse;
import com.example.gradox2.presentation.dto.files.UploadFileRequest;

public interface IFileService {

    List<FileResponse> getAllFiles();
    ResponseEntity uploadFile(UploadFileRequest dto);
}
