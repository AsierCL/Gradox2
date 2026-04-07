package com.example.gradox2.presentation.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;

import com.example.gradox2.presentation.dto.fileProposal.UploadFileProposalRequest;
import com.example.gradox2.presentation.dto.files.FileResponse;
import com.example.gradox2.presentation.dto.vote.VoteResponse;
import com.example.gradox2.service.interfaces.IFileService;

import java.util.List;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;



@RestController
@RequestMapping("/files")
@Validated
public class FileController {
    private final IFileService fileService;

    public FileController(IFileService fileService) {
        this.fileService = fileService;
    }

    @GetMapping("/all")
    public ResponseEntity<List<FileResponse>> getAllFiles() {
        return ResponseEntity.ok(fileService.getAllFiles());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FileResponse> getFile(@PathVariable @Positive Long id) {
        return ResponseEntity.ok(fileService.getFile(id));
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<ByteArrayResource> downloadFile(@PathVariable @Positive Long id) {
        return fileService.downloadFile(id);
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@Valid @ModelAttribute UploadFileProposalRequest uploadFileRequest){
        return fileService.uploadFile(uploadFileRequest);
    }

    @PostMapping("/{id}/vote/{upvote}")
    public ResponseEntity<VoteResponse> voteFile(@PathVariable @Positive Long id, @PathVariable boolean upvote) {
        return ResponseEntity.ok(fileService.voteFile(id, upvote));
    }

    @DeleteMapping("/{id}/vote")
    public ResponseEntity<VoteResponse> retractVote(@PathVariable @Positive Long id) {
        return ResponseEntity.ok(fileService.retractVote(id));
    }

}
