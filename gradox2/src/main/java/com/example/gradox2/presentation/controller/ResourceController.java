package com.example.gradox2.presentation.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.gradox2.presentation.dto.files.FileResponse;
import com.example.gradox2.service.interfaces.IResourceService;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/files")
public class ResourceController {
    private final IResourceService resourceService;

    public ResourceController(IResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @GetMapping("/files")
    public ResponseEntity<List<FileResponse>> getAllFiles() {
        return null;
    }
    
}
