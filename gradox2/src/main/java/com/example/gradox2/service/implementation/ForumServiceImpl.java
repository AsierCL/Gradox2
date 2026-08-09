package com.example.gradox2.service.implementation;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.gradox2.persistence.entities.File;
import com.example.gradox2.persistence.entities.ForumThread;
import com.example.gradox2.persistence.entities.ThreadComment;
import com.example.gradox2.persistence.entities.User;
import com.example.gradox2.persistence.repository.FileRepository;
import com.example.gradox2.persistence.repository.ForumThreadRepository;
import com.example.gradox2.persistence.repository.ThreadCommentRepository;
import com.example.gradox2.presentation.dto.thread.CommentResponse;
import com.example.gradox2.presentation.dto.thread.CreateCommentRequest;
import com.example.gradox2.service.exceptions.InvalidFileOperation;
import com.example.gradox2.service.exceptions.NotFoundException;
import com.example.gradox2.service.interfaces.IForumService;
import com.example.gradox2.utils.GetAuthUser;
import com.example.gradox2.utils.IdentityVisibility;
import com.example.gradox2.utils.mapper.ThreadMapper;

@Service
public class ForumServiceImpl implements IForumService {

    private static final int MAX_PAGE_SIZE = 100;

    private final ForumThreadRepository forumThreadRepository;
    private final ThreadCommentRepository commentRepository;
    private final FileRepository fileRepository;

    public ForumServiceImpl(ForumThreadRepository forumThreadRepository,
            ThreadCommentRepository commentRepository, FileRepository fileRepository) {
        this.forumThreadRepository = forumThreadRepository;
        this.commentRepository = commentRepository;
        this.fileRepository = fileRepository;
    }

    @Override
    @Transactional
    public CommentResponse createComment(Long fileId, CreateCommentRequest request) {
        User author = GetAuthUser.getAuthUser();
        File file = requireViewableFile(fileId, author);

        ForumThread thread = forumThreadRepository.findByFileId(fileId)
                .orElseGet(() -> forumThreadRepository.save(ForumThread.builder().file(file).build()));
        if (thread.isLocked()) {
            throw new InvalidFileOperation("The thread is locked");
        }

        ThreadComment comment = ThreadComment.builder()
                .thread(thread)
                .author(author)
                .content(request.getContent().trim())
                .referencedFile(resolveReferencedFile(request.getReferencedFileId(), author))
                .parent(resolveParent(request.getParentCommentId(), thread))
                .build();

        thread.addComment(comment);
        commentRepository.save(comment);
        return ThreadMapper.toCommentResponse(comment, author);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponse> getThreadComments(Long fileId, int page, int size) {
        User viewer = GetAuthUser.getCurrentUserOrNull();
        requireViewableFile(fileId, viewer);
        ForumThread thread = forumThreadRepository.findByFileId(fileId).orElse(null);
        if (thread == null) {
            return List.of();
        }

        return commentRepository
                .findByThreadIdOrderByCreatedAtAscIdAsc(thread.getId(),
                        PageRequest.of(page, Math.min(size, MAX_PAGE_SIZE)))
                .stream()
                .map(comment -> ThreadMapper.toCommentResponse(comment, viewer))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CommentResponse editComment(Long fileId, Long commentId, String newContent) {
        User author = GetAuthUser.getAuthUser();
        requireViewableFile(fileId, author);
        ThreadComment comment = requireComment(fileId, commentId);

        if (!IdentityVisibility.isSameUser(comment.getAuthor(), author)) {
            throw new InvalidFileOperation("Only the author can edit this comment");
        }

        comment.setContent(newContent.trim());
        comment.setEditedAt(Instant.now());
        return ThreadMapper.toCommentResponse(comment, author);
    }

    @Override
    @Transactional
    public void deleteComment(Long fileId, Long commentId) {
        User requester = GetAuthUser.getAuthUser();
        requireViewableFile(fileId, requester);
        ThreadComment comment = requireComment(fileId, commentId);

        if (!IdentityVisibility.isSameUser(comment.getAuthor(), requester) && !IdentityVisibility.isMaster(requester)) {
            throw new InvalidFileOperation("Only the author or a master can delete this comment");
        }

        ForumThread thread = comment.getThread();
        Set<ThreadComment> subtree = collectSubtree(comment);
        subtree.forEach(node -> {
            if (node.getParent() != null) {
                node.getParent().getReplies().remove(node);
            }
        });
        thread.getComments().removeAll(subtree);

        if (thread.getComments().isEmpty()) {
            forumThreadRepository.delete(thread);
        }
    }

    private Set<ThreadComment> collectSubtree(ThreadComment comment) {
        Set<ThreadComment> subtree = new LinkedHashSet<>();
        Deque<ThreadComment> stack = new ArrayDeque<>();
        stack.push(comment);
        while (!stack.isEmpty()) {
            ThreadComment current = stack.pop();
            if (!subtree.add(current)) {
                continue;
            }
            for (ThreadComment reply : current.getReplies()) {
                stack.push(reply);
            }
        }
        return subtree;
    }

    @Override
    @Transactional
    public void setThreadLocked(Long fileId, boolean locked) {
        User requester = GetAuthUser.getAuthUser();
        File file = requireViewableFile(fileId, requester);

        if (!IdentityVisibility.isSameUser(file.getUploader(), requester) && !IdentityVisibility.isMaster(requester)) {
            throw new InvalidFileOperation("Only the uploader or a master can lock the thread");
        }

        ForumThread thread = forumThreadRepository.findByFileId(fileId)
                .orElseThrow(() -> new NotFoundException("Thread not found"));
        thread.setLocked(locked);
    }

    private File requireViewableFile(Long fileId, User viewer) {
        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new NotFoundException("File not found"));
        if (!IdentityVisibility.canViewContent(file, viewer)) {
            throw new NotFoundException("File not found");
        }
        return file;
    }

    private File resolveReferencedFile(Long referencedFileId, User author) {
        if (referencedFileId == null) {
            return null;
        }
        return requireViewableFile(referencedFileId, author);
    }

    private ThreadComment resolveParent(Long parentCommentId, ForumThread thread) {
        if (parentCommentId == null) {
            return null;
        }
        return commentRepository.findByIdAndThreadId(parentCommentId, thread.getId())
                .orElseThrow(() -> new InvalidFileOperation("Parent comment not found in this thread"));
    }

    private ThreadComment requireComment(Long fileId, Long commentId) {
        ForumThread thread = forumThreadRepository.findByFileId(fileId)
                .orElseThrow(() -> new NotFoundException("Thread not found"));
        return commentRepository.findByIdAndThreadId(commentId, thread.getId())
                .orElseThrow(() -> new NotFoundException("Comment not found"));
    }
}