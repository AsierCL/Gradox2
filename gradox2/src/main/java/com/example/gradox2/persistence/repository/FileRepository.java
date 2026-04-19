package com.example.gradox2.persistence.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.gradox2.persistence.entities.File;
import com.example.gradox2.persistence.entities.enums.FileType;

public interface FileRepository extends JpaRepository<File, Long> {
    //Find methods
    List<File> findByTitle(String title);
    List<File> findByType(FileType type);
    List<File> findBySubjectId(Long subjectId);
    List<File> findByUploaderId(Long uploaderId);
    List<File> findByUploaderIdAndSubjectId(Long uploaderId, Long subjectId);
}
