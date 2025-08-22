package com.example.gradox2.presentation.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.gradox2.presentation.dto.files.FileResponse;
import com.example.gradox2.service.interfaces.IFileService;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/files")
public class FileController {
    private final IFileService resourceService;

    public FileController(IFileService resourceService) {
        this.resourceService = resourceService;
    }

    @GetMapping("/files")
    public ResponseEntity<List<FileResponse>> getAllFiles() {
        List<FileResponse> files = resourceService.getAllFiles();
        return ResponseEntity.ok(files);
    }

}
