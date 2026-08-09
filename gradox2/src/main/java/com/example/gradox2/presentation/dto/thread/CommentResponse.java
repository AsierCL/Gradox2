package com.example.gradox2.presentation.dto.thread;

import java.time.Instant;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CommentResponse {

    private Long id;
    private String content;
    private String authorUsername;
    private Long parentCommentId;
    private FileReference referencedFile;
    private Instant createdAt;
    private Instant editedAt;

    @Getter
    @Builder
    public static class FileReference {
        private Long id;
        private String title;
        private String fileType;
        private String subject;
        private boolean available;
    }
}