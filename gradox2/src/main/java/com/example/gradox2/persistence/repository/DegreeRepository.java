package com.example.gradox2.persistence.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.gradox2.persistence.entities.Degree;

public interface DegreeRepository extends JpaRepository<Degree, Long> {

    Optional<Degree> findByCode(String code);

    List<Degree> findAllByOrderByCodeAsc();
}
