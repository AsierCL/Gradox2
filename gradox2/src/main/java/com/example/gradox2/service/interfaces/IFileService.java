package com.example.gradox2.service.interfaces;

import java.util.List;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;

import com.example.gradox2.presentation.dto.fileProposal.UploadFileProposalRequest;
import com.example.gradox2.presentation.dto.files.FileResponse;
import com.example.gradox2.presentation.dto.vote.VoteResponse;

public interface IFileService {

    ResponseEntity<String> uploadFile(UploadFileProposalRequest dto);
    List<FileResponse> getAllFiles();
    FileResponse getFile(Long id);
    ResponseEntity<ByteArrayResource> downloadFile(Long id);
    VoteResponse voteFile(Long id, boolean upvote);
    VoteResponse retractVote(Long id);
}
