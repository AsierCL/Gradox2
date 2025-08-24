package com.example.gradox2.persistence.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.gradox2.persistence.entities.File;
import com.example.gradox2.persistence.entities.enums.FileType;

public interface FileRepository extends JpaRepository<File, Long> {
    //Find methods
    List<File> findAll();
    Optional<File> findById(Long id);
    List<File> findByTitle(String title);
    List<File> findByType(FileType type);
    List<File> findBySubjectId(Long subjectId);
    List<File> findByUploaderId(Long uploaderId);
    List<File> findByUploaderIdAndSubjectId(Long uploaderId, Long subjectId);

    //save, delete, exists methods
    File save(File resource);
    void deleteById(Long id);
    boolean existsById(Long id);
    long count();
}
