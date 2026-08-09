package com.example.gradox2.service.interfaces;

import java.util.List;

import com.example.gradox2.presentation.dto.thread.CommentResponse;
import com.example.gradox2.presentation.dto.thread.CreateCommentRequest;

public interface IForumService {

    CommentResponse createComment(Long fileId, CreateCommentRequest request);

    List<CommentResponse> getThreadComments(Long fileId, int page, int size);

    CommentResponse editComment(Long fileId, Long commentId, String newContent);

    void deleteComment(Long fileId, Long commentId);

    void setThreadLocked(Long fileId, boolean locked);
}