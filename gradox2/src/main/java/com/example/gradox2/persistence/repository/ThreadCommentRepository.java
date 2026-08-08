package com.example.gradox2.persistence.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.gradox2.persistence.entities.ThreadComment;

public interface ThreadCommentRepository extends JpaRepository<ThreadComment, Long> {

    List<ThreadComment> findByThreadIdOrderByCreatedAtAscIdAsc(Long threadId, Pageable pageable);

    Optional<ThreadComment> findByIdAndThreadId(Long id, Long threadId);
}