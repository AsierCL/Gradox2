package com.example.gradox2.service.implementation;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.gradox2.persistence.entities.Subject;
import com.example.gradox2.persistence.entities.TempFile;
import com.example.gradox2.persistence.entities.FileProposal;
import com.example.gradox2.persistence.entities.User;
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
import com.example.gradox2.service.interfaces.IFileProposalService;
import com.example.gradox2.utils.GetAuthUser;
import com.example.gradox2.utils.mapper.FileProposalMapper;

@Service
public class FileProposalServiceImpl implements IFileProposalService {

    private final TempFileRepository tempFileRepository;
    private final FileProposalRepository fileProposalRepository;
    private final SubjectRepository subjectRepository;

    public FileProposalServiceImpl(TempFileRepository tempFileRepository,
            FileProposalRepository fileProposalRepository, SubjectRepository subjectRepository,
            SecurityFilterChain filterChain) {
        this.tempFileRepository = tempFileRepository;
        this.fileProposalRepository = fileProposalRepository;
        this.subjectRepository = subjectRepository;
    }

    @Transactional
    public FileProposalResponse uploadFileProposal(UploadFileProposalRequest dto) {
        Subject subject = subjectRepository.findById(dto.getSubjectId())
                .orElseThrow(() -> new NotFoundException("Subject not found"));

        User uploader = GetAuthUser.getAuthUser();

        try {
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

            // TODO : FACER ESTO CON BUILDER E NON HARDCODEAR OS VALORES
            FileProposal proposal = new FileProposal();
            proposal.setProposer(uploader);
            proposal.setTempFile(tempFile);
            proposal.setStatus(ProposalStatus.PENDING);
            proposal.setQuorumRequired(5);
            proposal.setApprovalThreshold(0.6);
            fileProposalRepository.save(proposal);

            return FileProposalMapper.toFileProposalResponse(proposal);

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

        if(proposal.getProposer().equals(authUser) == false) {
            throw new InvalidFileOperation("Only the proposer can delete this proposal");
        }

        // Eliminar el TempFile asociado
        TempFile tempFile = proposal.getTempFile();
        if (tempFile != null) {
            tempFileRepository.delete(tempFile);
        }

        // Eliminar la propuesta
        fileProposalRepository.delete(proposal);

        return "Proposal and associated TempFile deleted.";
    }

    public List<FileProposalResponse> getAllFileProposals() {
        return fileProposalRepository.findAll().stream()
                .map(FileProposalMapper::toFileProposalResponse)
                .toList();
    }

    @Transactional
    public FileProposalResponse getFileProposalById(Long id) {
        return FileProposalMapper.toFileProposalResponse(fileProposalRepository.findById(id).orElseThrow(() -> new NotFoundException("Proposal not found")));
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
}
