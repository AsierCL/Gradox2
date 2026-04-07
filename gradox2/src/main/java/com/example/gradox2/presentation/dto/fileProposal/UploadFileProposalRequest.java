package com.example.gradox2.presentation.dto.fileProposal;

import org.springframework.web.multipart.MultipartFile;

import com.example.gradox2.persistence.entities.enums.FileType;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class UploadFileProposalRequest {
    private static final long MAX_FILE_SIZE_BYTES = 10 * 1024 * 1024;

    @NotBlank(message = "Title is required")
    @Size(max = 150, message = "Title must not exceed 150 characters")
    private String title;

    @NotBlank(message = "Description is required")
    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    @NotNull(message = "File type is required")
    private FileType type;

    @NotNull(message = "Subject id is required")
    @Positive(message = "Subject id must be positive")
    private Long subjectId;

    @NotNull(message = "File is required")
    private MultipartFile file;

    @AssertTrue(message = "File cannot be empty and must be <= 10MB")
    public boolean isValidFile() {
        return file != null && !file.isEmpty() && file.getSize() <= MAX_FILE_SIZE_BYTES;
    }
}
