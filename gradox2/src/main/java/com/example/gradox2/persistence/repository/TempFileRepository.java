package com.example.gradox2.persistence.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.gradox2.persistence.entities.TempFile;
import com.example.gradox2.persistence.entities.enums.FileType;

public interface TempFileRepository extends JpaRepository<TempFile, Long> {
    //Find methods
    List<TempFile> findAll();
    Optional<TempFile> findById(Long id);
    List<TempFile> findByTitle(String title);
    List<TempFile> findByType(FileType type);
    List<TempFile> findBySubjectId(Long subjectId);
    List<TempFile> findByUploaderId(Long uploaderId);
    List<TempFile> findByUploaderIdAndSubjectId(Long uploaderId, Long subjectId);

    //save, delete, exists methods
    TempFile save(TempFile resource);
    void deleteById(Long id);
    boolean existsById(Long id);
    long count();

}
