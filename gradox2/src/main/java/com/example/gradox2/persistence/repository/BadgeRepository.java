package com.example.gradox2.persistence.repository;

import java.util.List;

import com.example.gradox2.persistence.entities.Badge;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BadgeRepository extends JpaRepository<Badge, Long> {
    List<Badge> findAllByOrderByNameAsc();
}