package com.example.gradox2.persistence.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.gradox2.persistence.entities.Subject;

public interface SubjectRepository extends JpaRepository<Subject, Long> {

    @Query("select s from Subject s join fetch s.course c "
            + "order by c.degree.id asc, c.code asc, s.code asc")
    List<Subject> findAllByOrderByCourseCodeAscCodeAsc();

    @Query("select s from Subject s join fetch s.course c "
            + "where c.degree.id = :degreeId order by c.code asc, s.code asc")
    List<Subject> findAllByDegreeId(@Param("degreeId") Long degreeId);

    Optional<Subject> findByCode(String code);

}