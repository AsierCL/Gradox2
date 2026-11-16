package com.example.gradox2.persistence.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.gradox2.persistence.entities.Degree;

public interface DegreeRepository extends JpaRepository<Degree, Long> {

    Optional<Degree> findByCode(String code);

    List<Degree> findAllByOrderByCodeAsc();

    /** Grados con sus cursos precargados, para que la respuesta no haga una consulta por grado. */
    @Query("select distinct d from Degree d left join fetch d.courses order by d.code asc")
    List<Degree> findAllWithCoursesOrderedByCode();
}
