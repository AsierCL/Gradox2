package com.example.gradox2.utils.mapper;

import com.example.gradox2.persistence.entities.File;
import com.example.gradox2.persistence.entities.ThreadComment;
import com.example.gradox2.persistence.entities.User;
import com.example.gradox2.presentation.dto.thread.CommentResponse;
import com.example.gradox2.utils.IdentityVisibility;

public final class ThreadMapper {

    private ThreadMapper() {
    }

    public static final CommentResponse toCommentResponse(ThreadComment comment, User viewer) {
        CommentResponse.FileReference reference = null;
        if (comment.getReferencedFile() != null) {
            File referenced = comment.getReferencedFile();
            boolean available = IdentityVisibility.canViewContent(referenced, viewer);
            reference = CommentResponse.FileReference.builder()
                    .id(available ? referenced.getId() : null)
                    .title(available ? referenced.getTitle() : null)
                    .fileType(available && referenced.getType() != null ? referenced.getType().name() : null)
                    .subject(available && referenced.getSubject() != null ? referenced.getSubject().getName() : null)
                    .available(available)
                    .build();
        }

        return CommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .authorUsername(IdentityVisibility.resolveDisplayUsername(
                        comment.getAuthor(), viewer, comment.getThread().getFile().getVisibilityLevel()))
                .parentCommentId(comment.getParent() != null ? comment.getParent().getId() : null)
                .referencedFile(reference)
                .createdAt(comment.getCreatedAt())
                .editedAt(comment.getEditedAt())
                .build();
    }
}