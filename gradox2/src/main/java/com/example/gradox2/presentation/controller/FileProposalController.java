package com.example.gradox2.presentation.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;

import com.example.gradox2.persistence.entities.TempFile;
import com.example.gradox2.presentation.dto.fileProposal.FileProposalResponse;
import com.example.gradox2.presentation.dto.fileProposal.UploadFileProposalRequest;
import com.example.gradox2.service.interfaces.IFileProposalService;

import java.util.List;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;


@RestController
@RequestMapping("/uploadProposal")
@Validated
public class FileProposalController {

    private final IFileProposalService fileProposalService;

    public FileProposalController(IFileProposalService fileProposalService) {
        this.fileProposalService = fileProposalService;
    }

    @PostMapping("/upload")
    public ResponseEntity<FileProposalResponse> uploadProposal(@Valid @ModelAttribute UploadFileProposalRequest fileProposalRequest) {
        return ResponseEntity.ok(fileProposalService.uploadFileProposal(fileProposalRequest));
    }

    @GetMapping
    public ResponseEntity<?> getAllUploadProposals(
            @RequestParam(defaultValue = "false") Boolean paged,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = "id") String sortBy) {

        if (paged) {
            Page<FileProposalResponse> proposals = fileProposalService.getFileProposalsPaged(page, size, sortBy);
            return ResponseEntity.ok(proposals);
        } else {
            List<FileProposalResponse> proposals = fileProposalService.getAllFileProposals();
            return ResponseEntity.ok(proposals);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<FileProposalResponse> getUploadProposal(@PathVariable @Positive Long id) {
        return ResponseEntity.ok(fileProposalService.getFileProposalById(id));
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<ByteArrayResource> downloadProposalFile(@PathVariable @Positive Long id) {
        TempFile file = fileProposalService.downloadFileFromProposal(id);
        ByteArrayResource resource = new ByteArrayResource(file.getFileData());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + file.getTitle())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(file.getFileData().length)
                .body(resource);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUploadProposal(@PathVariable @Positive Long id) {
        return ResponseEntity.ok(fileProposalService.deleteFileProposal(id));
    }
}
