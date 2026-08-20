package com.example.gradox2.persistence.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.gradox2.persistence.entities.Subject;

public interface SubjectRepository extends JpaRepository<Subject, Long> {

    @Query("select s from Subject s join fetch s.course c order by c.code asc, s.code asc")
    List<Subject> findAllByOrderByCourseCodeAscCodeAsc();

    Optional<Subject> findByCode(String code);

}