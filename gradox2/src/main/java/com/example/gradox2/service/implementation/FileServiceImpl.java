package com.example.gradox2.service.implementation;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.stream.Collectors;

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
import com.example.gradox2.persistence.entities.enums.ProposalStatus;
import com.example.gradox2.persistence.repository.FileRepository;
import com.example.gradox2.persistence.repository.SubjectRepository;
import com.example.gradox2.persistence.repository.TempFileRepository;
import com.example.gradox2.persistence.repository.FileProposalRepository;
import com.example.gradox2.presentation.dto.files.FileResponse;
import com.example.gradox2.presentation.dto.files.UploadFileRequest;
import com.example.gradox2.service.exceptions.NotFoundException;
import com.example.gradox2.service.interfaces.IFileService;
import com.example.gradox2.utils.GetAuthUser;

@Service
public class FileServiceImpl implements IFileService {

    private final FileRepository fileRepository;
    private final TempFileRepository tempFileRepository;
    private final FileProposalRepository uploadProposalRepository;
    private final SubjectRepository subjectRepository;

    public FileServiceImpl(FileRepository fileRepository, TempFileRepository tempFileRepository,
            FileProposalRepository uploadProposalRepository, SubjectRepository subjectRepository,
            SecurityFilterChain filterChain) {
        this.fileRepository = fileRepository;
        this.tempFileRepository = tempFileRepository;
        this.uploadProposalRepository = uploadProposalRepository;
        this.subjectRepository = subjectRepository;
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
    public ResponseEntity<String> uploadFile(UploadFileRequest dto) {
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

            // 4. Crear UploadProposal asociada al TempFile
            FileProposal proposal = new FileProposal();
            proposal.setProposer(uploader);
            proposal.setTempFile(tempFile);
            proposal.setStatus(ProposalStatus.PENDING);
            proposal.setQuorumRequired(5);
            proposal.setApprovalThreshold(0.6);
            uploadProposalRepository.save(proposal);

            // 5. Devolver respuesta
            return ResponseEntity.ok("Archivo enviado para revisión. ID de propuesta: " + proposal.getId());

        } catch (IOException e) {
            return ResponseEntity.status(500).body("Error al procesar el archivo: " + e.getMessage());
        }
    }

    public List<FileResponse> getAllFiles() {
        return fileRepository.findAll().stream()
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

    public FileResponse getFile(Long id) {
        File file = fileRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("File not found"));

        return FileResponse.builder()
                .id(file.getId())
                .fileName(file.getTitle())
                .description(file.getDescription())
                .fileType(file.getType())
                .subject(file.getSubject().getName())
                .uploaderUsername(file.getUploader().getUsername())
                .build();
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
}
