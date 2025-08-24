package com.example.gradox2.service.implementation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.example.gradox2.persistence.entities.File;
import com.example.gradox2.persistence.entities.Subject;
import com.example.gradox2.persistence.entities.User;
import com.example.gradox2.persistence.entities.enums.FileType;
import com.example.gradox2.persistence.repository.FileRepository;
import com.example.gradox2.persistence.repository.SubjectRepository;
import com.example.gradox2.presentation.dto.files.FileResponse;
import com.example.gradox2.presentation.dto.files.UploadFileRequest;
import com.example.gradox2.service.exceptions.NotFoundException;
import com.example.gradox2.service.exceptions.UnauthenticatedAccessException;
import com.example.gradox2.service.interfaces.IFileService;


@Service
public class FileServiceImpl implements IFileService {

    private final FileRepository fileRepository;
    private final SubjectRepository subjectRepository;
    private final PasswordEncoder passwordEncoder;

    public FileServiceImpl(FileRepository fileRepository, SubjectRepository subjectRepository, PasswordEncoder passwordEncoder, SecurityFilterChain filterChain) {
        this.fileRepository = fileRepository;
        this.subjectRepository = subjectRepository;
        this.passwordEncoder = passwordEncoder;
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

    public ResponseEntity uploadFile(UploadFileRequest dto) {
        Subject subject = subjectRepository.findById(dto.getSubjectId())
                .orElseThrow(() -> new NotFoundException("Subject not found"));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthenticatedAccessException("El usuario no está autenticado.");
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof User)) {
            throw new UnauthenticatedAccessException("El usuario no es una instancia de User.");
        }

        try {
            File file = File.builder()
                    .title(dto.getTitle())
                    .description(dto.getDescription())
                    .type(dto.getType())
                    .fileData(dto.getFile().getBytes()) // Guardamos como byte[]
                    .fileHash(passwordEncoder.encode(dto.getFile().getBytes().toString()))
                    .subject(subject)
                    .uploader((User) principal)
                    .build();
            fileRepository.save(file);

            return ResponseEntity.ok("File uploaded successfully");
        } catch (java.io.IOException e) {
            return ResponseEntity.status(500).body("File upload failed: " + e.getMessage());
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
                        .build()
                ).collect(Collectors.toList());
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
}
