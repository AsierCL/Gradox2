package com.example.gradox2.persistence.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.gradox2.persistence.entities.File;
import com.example.gradox2.persistence.entities.enums.FileType;

public interface FileRepository extends JpaRepository<File, Long> {
    //Find methods
    List<File> findByTitle(String title);
    List<File> findByType(FileType type);
    List<File> findBySubjectId(Long subjectId);
    List<File> findByUploaderId(Long uploaderId);
    List<File> findByUploaderIdAndSubjectId(Long uploaderId, Long subjectId);
    List<File> findBySizeBytesIsNull();

    //Stats
    @Query("SELECT COALESCE(SUM(f.sizeBytes), 0) FROM File f")
    Long sumSizeBytes();

    @Query("SELECT COALESCE(SUM(f.downloadCount), 0) FROM File f")
    Long sumDownloadCount();

    @Query("SELECT f.type AS type, COUNT(f) AS count FROM File f GROUP BY f.type ORDER BY COUNT(f) DESC")
    List<FileTypeCount> countByType();

    @Query("SELECT s.code AS code, s.name AS name, c.name AS courseName, COUNT(f) AS count "
            + "FROM Subject s "
            + "LEFT JOIN s.resources f "
            + "LEFT JOIN s.course c "
            + "GROUP BY s.id, s.code, s.name, c.name "
            + "ORDER BY COUNT(f) DESC")
    List<SubjectFileCount> countBySubject();

    interface FileTypeCount {
        FileType getType();
        long getCount();
    }

    interface SubjectFileCount {
        String getCode();
        String getName();
        String getCourseName();
        long getCount();
    }
}
