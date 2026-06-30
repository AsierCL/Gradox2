package com.example.gradox2.presentation.dto.fileProposal;

import org.springframework.web.multipart.MultipartFile;

import com.example.gradox2.persistence.entities.enums.FileType;
import com.example.gradox2.persistence.entities.enums.FileVisibility;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Schema(description = "Solicitud de propuesta de subida de archivo (multipart/form-data)")
public class UploadFileProposalRequest {
    private static final long MAX_FILE_SIZE_BYTES = 10 * 1024 * 1024;

    @NotBlank(message = "Title is required")
    @Size(max = 150, message = "Title must not exceed 150 characters")
    @Schema(description = "Título del archivo", example = "Apuntes Tema 1")
    private String title;

    @NotBlank(message = "Description is required")
    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    @Schema(description = "Descripción del archivo", example = "Apuntes completos del tema 1")
    private String description;

    @NotNull(message = "File type is required")
    @Schema(description = "Tipo de archivo (APUNTES, EJERCICIO, EXAMEN)")
    private FileType type;

    @NotNull(message = "Subject id is required")
    @Positive(message = "Subject id must be positive")
    @Schema(description = "ID de la asignatura", example = "1")
    private Long subjectId;

    @NotNull(message = "File is required")
    @Schema(description = "Archivo a subir (PDF, máx 10 MB)")
    private MultipartFile file;

    @Builder.Default
    @Schema(description = "Nivel de visibilidad del uploader (PUBLIC, RESTRICTED, PRIVATE)", example = "PUBLIC")
    private FileVisibility visibilityLevel = FileVisibility.PUBLIC;

    @AssertTrue(message = "File cannot be empty and must be <= 10MB")
    public boolean isValidFile() {
        return file != null && !file.isEmpty() && file.getSize() <= MAX_FILE_SIZE_BYTES;
    }
}
