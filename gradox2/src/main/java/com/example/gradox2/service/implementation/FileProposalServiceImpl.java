package com.example.gradox2.service.implementation;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.gradox2.persistence.entities.Subject;
import com.example.gradox2.persistence.entities.TempFile;
import com.example.gradox2.persistence.entities.FileProposal;
import com.example.gradox2.persistence.entities.User;
import com.example.gradox2.persistence.entities.GlobalConfig;
import com.example.gradox2.persistence.entities.enums.ActionType;
import com.example.gradox2.persistence.entities.enums.FileVisibility;
import com.example.gradox2.persistence.entities.enums.ProposalStatus;
import com.example.gradox2.persistence.repository.SubjectRepository;
import com.example.gradox2.persistence.repository.TempFileRepository;
import com.example.gradox2.presentation.dto.fileProposal.FileProposalResponse;
import com.example.gradox2.presentation.dto.fileProposal.UploadFileProposalRequest;
import com.example.gradox2.persistence.repository.FileProposalRepository;
import com.example.gradox2.service.exceptions.InternalServerErrorException;
import com.example.gradox2.service.exceptions.InvalidFileOperation;
import com.example.gradox2.service.exceptions.NotFoundException;
import com.example.gradox2.service.exceptions.ProposalClosedException;
import com.example.gradox2.service.interfaces.FileUrlSigner;
import com.example.gradox2.service.interfaces.IAuditService;
import com.example.gradox2.service.interfaces.IFileProposalService;
import com.example.gradox2.service.interfaces.IGlobalConfigService;
import com.example.gradox2.utils.ContentDisposition;
import com.example.gradox2.utils.GetAuthUser;
import com.example.gradox2.utils.SortUtils;
import com.example.gradox2.utils.mapper.FileProposalMapper;
import com.example.gradox2.presentation.dto.files.FileDownloadResponse;

@Service
public class FileProposalServiceImpl implements IFileProposalService {
    private static final int MAX_PAGE_SIZE = 100;
    // Ten que haber algunha forma mais elegante, non se me ocurreu
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "id",
            "quorumRequired",
            "approvalThreshold",
            "status",
            "actionType",
            "createdAt",
            "closedAt");

    private final TempFileRepository tempFileRepository;
    private final FileProposalRepository fileProposalRepository;
    private final SubjectRepository subjectRepository;
    private final IGlobalConfigService voteConfigService;
    private final S3StorageService s3StorageService;
    private final FileUrlSigner fileUrlSigner;
    private final IAuditService auditService;

    public FileProposalServiceImpl(TempFileRepository tempFileRepository,
            FileProposalRepository fileProposalRepository, SubjectRepository subjectRepository,
            IGlobalConfigService voteConfigService, S3StorageService s3StorageService,
            FileUrlSigner fileUrlSigner, IAuditService auditService) {
        this.tempFileRepository = tempFileRepository;
        this.fileProposalRepository = fileProposalRepository;
        this.subjectRepository = subjectRepository;
        this.voteConfigService = voteConfigService;
        this.s3StorageService = s3StorageService;
        this.fileUrlSigner = fileUrlSigner;
        this.auditService = auditService;
    }

    @Transactional
    public FileProposalResponse uploadFileProposal(UploadFileProposalRequest dto) {
        Subject subject = subjectRepository.findById(dto.getSubjectId())
                .orElseThrow(() -> new NotFoundException("Subject not found"));

        User uploader = GetAuthUser.getAuthUser();
        GlobalConfig config = voteConfigService.getConfig();
        ensurePendingUploadLimit(uploader, config.getMaxPendingUploads());

        try {
            byte[] fileData = dto.getFile().getBytes();
            String objectKey = s3StorageService.put(fileData);
            TempFile tempFile = TempFile.builder()
                    .title(dto.getTitle())
                    .description(dto.getDescription())
                    .type(dto.getType())
                    .objectKey(objectKey)
                    .fileHash(generateFileHash(fileData)) // Hash seguro
                    .sizeBytes((long) fileData.length)
                    .subject(subject)
                    .uploader(uploader)
                    .visibilityLevel(dto.getVisibilityLevel() != null ? dto.getVisibilityLevel() : FileVisibility.PUBLIC)
                    .build();
            tempFileRepository.save(tempFile);

            FileProposal proposal = new FileProposal();
            proposal.setProposer(uploader);
            proposal.setTempFile(tempFile);
            proposal.setStatus(ProposalStatus.PENDING);
            proposal.setActionType(ActionType.UPLOAD);
            proposal.setQuorumRequired(config.getQuorumRequired());
            proposal.setApprovalThreshold(config.getApprovalThreshold());
            fileProposalRepository.save(proposal);

            auditService.record(ActionType.UPLOAD, "FileProposal", proposal.getId(), "Propuesta de subida creada");

            return FileProposalMapper.toFileProposalResponse(proposal, uploader);

        } catch (IOException e) {
            throw new InternalServerErrorException("Error processing file", e);
        }
    }

    public String deleteFileProposal(Long id) {
        User authUser = GetAuthUser.getAuthUser();

        FileProposal proposal = fileProposalRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Proposal not found"));

        if(proposal.getStatus() != ProposalStatus.PENDING) {
            throw new ProposalClosedException("Only pending proposals can be deleted");
        }

        if (!Objects.equals(proposal.getProposer().getId(), authUser.getId())) {
            throw new InvalidFileOperation("Only the proposer can delete this proposal");
        }

        TempFile tempFile = proposal.getTempFile();
        if (tempFile != null) {
            s3StorageService.delete(tempFile.getObjectKey());
        }

        // Let JPA cascading/orphan removal handle TempFile cleanup safely.
        fileProposalRepository.delete(proposal);

        return "Proposal and associated TempFile deleted.";
    }

    public List<FileProposalResponse> getAllFileProposals() {
        return getFileProposalsPaged(0, MAX_PAGE_SIZE, "id").getContent();
    }

    public Page<FileProposalResponse> getFileProposalsPaged(int page, int size, String sortBy) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        String safeSortBy = SortUtils.resolveSortBy(sortBy, "id", ALLOWED_SORT_FIELDS);
        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(safeSortBy).descending());
        Page<FileProposal> proposalPage = fileProposalRepository.findAll(pageable);
        User viewer = getCurrentViewerOrNull();
        return proposalPage.map(proposal -> FileProposalMapper.toFileProposalResponse(proposal, viewer));
    }

    @Transactional
    public FileProposalResponse getFileProposalById(Long id) {
        return FileProposalMapper.toFileProposalResponse(fileProposalRepository.findById(id).orElseThrow(() -> new NotFoundException("Proposal not found")), getCurrentViewerOrNull());
    }

    public TempFile downloadFileFromProposal(Long id) {
        FileProposal proposal = fileProposalRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Proposal not found"));

        if (proposal.getStatus() != ProposalStatus.PENDING) {
            throw new NotFoundException("Proposal is not pending");
        }

        TempFile tempFile = proposal.getTempFile();
        if (tempFile == null) {
            throw new NotFoundException("No TempFile associated with this proposal");
        }

        return tempFile;
    }

    public FileDownloadResponse getFileDownloadLink(Long id) {
        TempFile tempFile = downloadFileFromProposal(id);
        String url = fileUrlSigner.presignedGetUrl(tempFile.getObjectKey(),
                ContentDisposition.attachmentOf(tempFile.getTitle()));
        return FileDownloadResponse.builder().url(url).build();
    }

    private String generateFileHash(byte[] fileData) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(fileData);
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error generating file hash", e);
        }
    }

    private User getCurrentViewerOrNull() {
        org.springframework.security.core.Authentication authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof User user)) {
            return null;
        }
        return user;
    }

    private void ensurePendingUploadLimit(User uploader, Integer maxPendingUploads) {
        long pendingUploads = fileProposalRepository.countByProposerAndStatusAndActionType(
                uploader,
                ProposalStatus.PENDING,
                ActionType.UPLOAD);

        if (maxPendingUploads != null && pendingUploads >= maxPendingUploads) {
            throw new InvalidFileOperation("Maximum pending uploads reached");
        }
    }
}
