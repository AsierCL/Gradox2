package com.example.gradox2.persistence.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.gradox2.persistence.entities.Resource;
import com.example.gradox2.persistence.entities.enums.ResourceType;

public interface ResourceRepository extends JpaRepository<Resource, Long> {
    //Find methods
    List<Resource> findAll();
    Optional<Resource> findById(Long id);
    List<Resource> findByTitle(String title);
    List<Resource> findByType(ResourceType type);
    List<Resource> findBySubjectId(Long subjectId);
    List<Resource> findByUploaderId(Long uploaderId);
    List<Resource> findByUploaderIdAndSubjectId(Long uploaderId, Long subjectId);
    
    //save, delete, exists methods
    Resource save(Resource resource);
    void deleteById(Long id);
    boolean existsById(Long id);
    long count();
}
