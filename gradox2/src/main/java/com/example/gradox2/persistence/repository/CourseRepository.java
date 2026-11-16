package com.example.gradox2.persistence.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.gradox2.persistence.entities.Course;

public interface CourseRepository extends JpaRepository<Course, Long> {

    // `distinct` + `order by` exige que la expresión de orden esté proyectada: se ordena por
    // `degree.id` (que sí lo está) y el orden definitivo por código de grado lo fija el servicio.
    @Query("select distinct c from Course c left join fetch c.subjects s "
            + "order by c.degree.id asc, c.code asc, s.code asc")
    List<Course> findAllWithSubjectsByOrderByCodeAsc();

    @Query("select distinct c from Course c left join fetch c.subjects s "
            + "where c.degree.id = :degreeId order by c.code asc, s.code asc")
    List<Course> findAllWithSubjectsByDegreeId(@Param("degreeId") Long degreeId);

    @Query("select distinct c from Course c left join fetch c.subjects s where c.id = :id")
    Optional<Course> findByIdWithSubjects(@Param("id") Long id);

}