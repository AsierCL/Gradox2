package com.example.gradox2.persistence.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.gradox2.persistence.entities.ForumThread;

public interface ForumThreadRepository extends JpaRepository<ForumThread, Long> {

    Optional<ForumThread> findByFileId(Long fileId);

    void deleteByFileId(Long fileId);
}