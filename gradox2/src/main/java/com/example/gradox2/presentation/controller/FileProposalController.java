package com.example.gradox2.presentation.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.gradox2.persistence.entities.FileProposal;
import com.example.gradox2.presentation.dto.files.UploadFileRequest;
import com.example.gradox2.service.interfaces.IFileProposalService;

import java.util.List;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;


@RestController
@RequestMapping("/uploadProposal")
public class FileProposalController {

    private final IFileProposalService fileProposalService;

    public FileProposalController(IFileProposalService fileProposalService) {
        this.fileProposalService = fileProposalService;
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadProposal(@ModelAttribute UploadFileRequest fileProposalRequest) {
        return fileProposalService.uploadFileProposal(fileProposalRequest);
    }

    // TODO Devolver otro tipo de dato
    @GetMapping("/all")
    public ResponseEntity<List<FileProposal>> getAllUploadProposals() {
        return ResponseEntity.ok(fileProposalService.getAllFileProposals());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FileProposal> getUploadProposal(@PathVariable Long id) {
        return ResponseEntity.ok(fileProposalService.getFileProposalById(id));
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<ByteArrayResource> downloadProposalFile(@PathVariable Long id) {
        return fileProposalService.downloadFileFromProposal(id);
    }

    @GetMapping("/{id}/delete")
    public ResponseEntity<String> deleteUploadProposal(@PathVariable Long id) {
        return fileProposalService.deleteFileProposal(id);
    }
}
