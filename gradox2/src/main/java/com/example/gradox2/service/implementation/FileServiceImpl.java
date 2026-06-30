package com.example.gradox2.service.implementation;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.gradox2.persistence.entities.File;
import com.example.gradox2.persistence.entities.FileProposal;
import com.example.gradox2.persistence.entities.User;
import com.example.gradox2.persistence.entities.GlobalConfig;
import com.example.gradox2.persistence.entities.Score;
import com.example.gradox2.persistence.entities.enums.ActionType;
import com.example.gradox2.persistence.entities.enums.FileVisibility;
import com.example.gradox2.persistence.entities.enums.ProposalStatus;
import com.example.gradox2.persistence.repository.FileRepository;
import com.example.gradox2.persistence.repository.ScoreRepository;
import com.example.gradox2.persistence.repository.FileProposalRepository;
import com.example.gradox2.service.interfaces.IGlobalConfigService;
import com.example.gradox2.presentation.dto.files.FileResponse;
import com.example.gradox2.presentation.dto.vote.VoteResponse;
import com.example.gradox2.service.exceptions.InvalidFileOperation;
import com.example.gradox2.service.exceptions.NotFoundException;
import com.example.gradox2.service.interfaces.IFileService;
import com.example.gradox2.utils.GetAuthUser;
import com.example.gradox2.utils.IdentityVisibility;
import com.example.gradox2.utils.mapper.FileMapper;

@Service
public class FileServiceImpl implements IFileService {
    private static final int MAX_LIST_SIZE = 100;

    private final FileRepository fileRepository;
    private final FileProposalRepository uploadProposalRepository;
    private final ScoreRepository scoreRepository;
    private final IGlobalConfigService voteConfigService;

    public FileServiceImpl(FileRepository fileRepository,
            FileProposalRepository uploadProposalRepository,
            IGlobalConfigService voteConfigService, ScoreRepository scoreRepository) {
        this.fileRepository = fileRepository;
        this.uploadProposalRepository = uploadProposalRepository;
        this.scoreRepository = scoreRepository;
        this.voteConfigService = voteConfigService;
    }

    public ResponseEntity<ByteArrayResource> downloadFile(Long id) {
        File file = fileRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("File not found"));

        ByteArrayResource resource = new ByteArrayResource(file.getFileData());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getTitle() + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(file.getFileData().length)
                .body(resource);
    }

    public List<FileResponse> getAllFiles() {
        User viewer = getCurrentViewerOrNull();
        return fileRepository.findAll(PageRequest.of(0, MAX_LIST_SIZE, Sort.by("id").descending())).getContent().stream()
            .map(file -> FileMapper.toFileResponse(file, viewer))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public FileResponse getFile(Long id) {
        File file = fileRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("File not found"));

        return FileMapper.toFileResponse(file, getCurrentViewerOrNull());
    }

    @Transactional
    public com.example.gradox2.presentation.dto.fileProposal.FileProposalResponse requestFileDeletion(Long id) {
        File file = fileRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("File not found"));

        if (uploadProposalRepository.existsByFileAndStatus(file, ProposalStatus.PENDING)) {
            throw new InvalidFileOperation("An active deletion proposal already exists for this file");
        }

        User requester = GetAuthUser.getAuthUser();
        GlobalConfig config = voteConfigService.getConfig();

        FileProposal proposal = new FileProposal();
        proposal.setProposer(requester);
        proposal.setFile(file);
        proposal.setStatus(ProposalStatus.PENDING);
        proposal.setActionType(ActionType.DELETE);
        proposal.setQuorumRequired(config.getQuorumRequired());
        proposal.setApprovalThreshold(config.getApprovalThreshold());

        uploadProposalRepository.save(proposal);
        return com.example.gradox2.utils.mapper.FileProposalMapper.toFileProposalResponse(proposal, requester);
    }

    @Transactional
    public VoteResponse voteFile(Long id, boolean upvote) {
        File file = fileRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("File not found"));

        User user = GetAuthUser.getAuthUser();

        // Comprobar si ya existe voto
        Score existingScore = scoreRepository.findByFileAndUser(file, user);

        if (existingScore != null) {
            // Si el usuario intenta votar lo mismo otra vez, devolver mensaje coherente
            if ((upvote && existingScore.getScore() == 1.0) ||
                (!upvote && existingScore.getScore() == -1.0)) {
                throw new InvalidFileOperation("Already voted this way");
            }

            // Si quiere cambiar el voto, actualizarlo
            existingScore.setScore(upvote ? 1.0 : -1.0);
            scoreRepository.save(existingScore);
            fileRepository.save(file);
            return new VoteResponse(existingScore.getId(), id, user.getUsername(), upvote, existingScore.getScoredAt());
        }

        // Nuevo voto
        Score score = Score.builder()
                .user(user)
                .file(file)
                .score(upvote ? 1.0 : -1.0)
                .build();

        file.addScore(score);
        scoreRepository.save(score);
        fileRepository.save(file);

        return new VoteResponse(score.getId(), id, user.getUsername(), upvote, score.getScoredAt());
    }


    @Transactional
    public VoteResponse retractVote(Long id) {
        // Obtener el archivo
        File file = fileRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("File not found"));

        // Obtener el usuario autenticado
        User user = GetAuthUser.getAuthUser();

        // Buscar el voto existente
        Score existingScore = scoreRepository.findByFileAndUser(file, user);
        if (existingScore == null) {
            throw new NotFoundException("No existing vote to retract");
        }

        // Eliminar el score
        file.removeScore(existingScore);
        scoreRepository.delete(existingScore);
        fileRepository.save(file);

        return new VoteResponse(existingScore.getId(), id, user.getUsername(), false, existingScore.getScoredAt());
    }

    @Transactional
    public FileResponse updateFileVisibility(Long id, FileVisibility visibilityLevel) {
        File file = fileRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("File not found"));

        User requester = GetAuthUser.getAuthUser();

        if (!IdentityVisibility.isSameUser(file.getUploader(), requester) && !IdentityVisibility.isMaster(requester)) {
            throw new InvalidFileOperation("Only the uploader or a master can change visibility");
        }

        file.setVisibilityLevel(visibilityLevel);
        fileRepository.save(file);

        return FileMapper.toFileResponse(file, requester);
    }

    private User getCurrentViewerOrNull() {
        Authentication authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof User user)) {
            return null;
        }
        return user;
    }
}
