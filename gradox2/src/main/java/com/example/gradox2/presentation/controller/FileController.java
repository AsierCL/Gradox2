package com.example.gradox2.presentation.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.gradox2.presentation.dto.files.FileResponse;
import com.example.gradox2.presentation.dto.files.UploadFileRequest;
import com.example.gradox2.service.interfaces.IFileService;

import io.jsonwebtoken.io.IOException;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;


@RestController
@RequestMapping("/files")
public class FileController {
    private final IFileService fileService;

    public FileController(IFileService fileService) {
        this.fileService = fileService;
    }

    @GetMapping("/files")
    public ResponseEntity<List<FileResponse>> getAllFiles() {
        List<FileResponse> files = fileService.getAllFiles();
        return ResponseEntity.ok(files);
    }

    @PostMapping("/upload")
    public ResponseEntity uploadFile(@ModelAttribute UploadFileRequest dto){
        return fileService.uploadFile(dto);
    }
}
