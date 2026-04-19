package com.example.gradox2.service.implementation;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.gradox2.persistence.entities.File;
import com.example.gradox2.persistence.entities.Subject;
import com.example.gradox2.persistence.entities.TempFile;
import com.example.gradox2.persistence.entities.FileProposal;
import com.example.gradox2.persistence.entities.User;
import com.example.gradox2.persistence.entities.VoteConfig;
import com.example.gradox2.persistence.entities.Score;
import com.example.gradox2.persistence.entities.enums.ActionType;
import com.example.gradox2.persistence.entities.enums.ProposalStatus;
import com.example.gradox2.persistence.repository.FileRepository;
import com.example.gradox2.persistence.repository.SubjectRepository;
import com.example.gradox2.persistence.repository.TempFileRepository;
import com.example.gradox2.persistence.repository.ScoreRepository;
import com.example.gradox2.persistence.repository.FileProposalRepository;
import com.example.gradox2.service.interfaces.IVoteConfigService;
import com.example.gradox2.presentation.dto.fileProposal.UploadFileProposalRequest;
import com.example.gradox2.presentation.dto.files.FileResponse;
import com.example.gradox2.presentation.dto.vote.VoteResponse;
import com.example.gradox2.service.exceptions.InvalidFileOperation;
import com.example.gradox2.service.exceptions.NotFoundException;
import com.example.gradox2.service.interfaces.IFileService;
import com.example.gradox2.utils.GetAuthUser;
import com.example.gradox2.utils.mapper.FileMapper;

@Service
public class FileServiceImpl implements IFileService {
    private static final int MAX_LIST_SIZE = 100;
    private static final Logger logger = LoggerFactory.getLogger(FileServiceImpl.class);

    private final FileRepository fileRepository;
    private final TempFileRepository tempFileRepository;
    private final FileProposalRepository uploadProposalRepository;
    private final SubjectRepository subjectRepository;
    private final ScoreRepository scoreRepository;
    private final IVoteConfigService voteConfigService;

    public FileServiceImpl(FileRepository fileRepository, TempFileRepository tempFileRepository,
            FileProposalRepository uploadProposalRepository, SubjectRepository subjectRepository,
            IVoteConfigService voteConfigService, SecurityFilterChain filterChain, ScoreRepository scoreRepository) {
        this.fileRepository = fileRepository;
        this.tempFileRepository = tempFileRepository;
        this.uploadProposalRepository = uploadProposalRepository;
        this.subjectRepository = subjectRepository;
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

    @Transactional
    public ResponseEntity<String> uploadFile(UploadFileProposalRequest dto) {
        // 1. Buscar la materia
        Subject subject = subjectRepository.findById(dto.getSubjectId())
                .orElseThrow(() -> new NotFoundException("Subject not found"));

        // 2. Obtener usuario autenticado
        User uploader = GetAuthUser.getAuthUser();

        try {
            // 3. Crear TempFile con el archivo subido
            TempFile tempFile = TempFile.builder()
                    .title(dto.getTitle())
                    .description(dto.getDescription())
                    .type(dto.getType())
                    .fileData(dto.getFile().getBytes())
                    .fileHash(generateFileHash(dto.getFile().getBytes())) // Hash seguro
                    .subject(subject)
                    .uploader(uploader)
                    .build();
            tempFileRepository.save(tempFile);

            VoteConfig config = voteConfigService.getConfig();
            // 4. Crear UploadProposal asociada al TempFile
            FileProposal proposal = new FileProposal();
            proposal.setProposer(uploader);
            proposal.setTempFile(tempFile);
            proposal.setStatus(ProposalStatus.PENDING);
            proposal.setActionType(ActionType.UPLOAD);
            proposal.setQuorumRequired(config.getQuorumRequired());
            proposal.setApprovalThreshold(config.getApprovalThreshold());
            uploadProposalRepository.save(proposal);

            // 5. Devolver respuesta
            return ResponseEntity.ok("Archivo enviado para revisión. ID de propuesta: " + proposal.getId());

        } catch (IOException e) {
            logger.error("File upload failed for subjectId={} title={}", dto.getSubjectId(), dto.getTitle(), e);
            return ResponseEntity.status(500).body("An unexpected error occurred while processing the file.");
        }
    }

    public List<FileResponse> getAllFiles() {
        return fileRepository.findAll(PageRequest.of(0, MAX_LIST_SIZE, Sort.by("id").descending())).getContent().stream()
                .map(file -> FileResponse.builder()
                        .id(file.getId())
                        .fileName(file.getTitle())
                        .description(file.getDescription())
                        .fileType(file.getType())
                        .subject(file.getSubject().getName())
                        .uploaderUsername(file.getUploader().getUsername())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public FileResponse getFile(Long id) {
        File file = fileRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("File not found"));

        return FileMapper.toFileResponse(file);
    }

    @Transactional
    public com.example.gradox2.presentation.dto.fileProposal.FileProposalResponse requestFileDeletion(Long id) {
        File file = fileRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("File not found"));

        if (uploadProposalRepository.existsByFileAndStatus(file, ProposalStatus.PENDING)) {
            throw new InvalidFileOperation("An active deletion proposal already exists for this file");
        }

        User requester = GetAuthUser.getAuthUser();
        VoteConfig config = voteConfigService.getConfig();

        FileProposal proposal = new FileProposal();
        proposal.setProposer(requester);
        proposal.setFile(file);
        proposal.setStatus(ProposalStatus.PENDING);
        proposal.setActionType(ActionType.DELETE);
        proposal.setQuorumRequired(config.getQuorumRequired());
        proposal.setApprovalThreshold(config.getApprovalThreshold());

        uploadProposalRepository.save(proposal);
        return com.example.gradox2.utils.mapper.FileProposalMapper.toFileProposalResponse(proposal);
    }

    private String generateFileHash(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(data);
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1)
                    hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error generating file hash", e);
        }
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
            return new VoteResponse(id, existingScore.getId(), user.getUsername(), upvote, existingScore.getScoredAt());
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

        return new VoteResponse(id, score.getId(), user.getUsername(), upvote, score.getScoredAt());
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

        return new VoteResponse(id, existingScore.getId(), user.getUsername(), false, existingScore.getScoredAt());
    }
}
