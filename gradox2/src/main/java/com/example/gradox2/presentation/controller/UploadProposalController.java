package com.example.gradox2.presentation.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.gradox2.persistence.entities.UploadProposal;
import com.example.gradox2.presentation.dto.files.FileResponse;
import com.example.gradox2.presentation.dto.files.UploadFileRequest;
import com.example.gradox2.service.interfaces.IUploadProposalService;

import java.util.List;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/uploadProposal")
public class UploadProposalController {

    private final IUploadProposalService uploadProposalService;

    public UploadProposalController(IUploadProposalService uploadProposalService) {
        this.uploadProposalService = uploadProposalService;
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadProposal(@ModelAttribute UploadFileRequest uploadFileRequest) {
        return uploadProposalService.uploadProposal(uploadFileRequest);
    }

    @GetMapping("/all")
    public ResponseEntity<List<UploadProposal>> getAllUploadProposals() {
        return ResponseEntity.ok(uploadProposalService.getAllProposals());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UploadProposal> getUploadProposal(@PathVariable Long id) {
        return ResponseEntity.ok(uploadProposalService.getProposalById(id));
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<ByteArrayResource> downloadProposalFile(@PathVariable Long id) {
        return uploadProposalService.downloadProposalFile(id);
    }

    @GetMapping("/{id}/delete")
    public ResponseEntity<String> deleteUploadProposal(@PathVariable Long id) {
        return uploadProposalService.deleteProposal(id);
    }
}
