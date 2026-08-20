package com.example.gradox2.persistence.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.gradox2.persistence.entities.Course;

public interface CourseRepository extends JpaRepository<Course, Long> {

    @Query("select distinct c from Course c left join fetch c.subjects s order by c.code asc, s.code asc")
    List<Course> findAllWithSubjectsByOrderByCodeAsc();

    @Query("select distinct c from Course c left join fetch c.subjects s where c.id = :id")
    Optional<Course> findByIdWithSubjects(@Param("id") Long id);

}