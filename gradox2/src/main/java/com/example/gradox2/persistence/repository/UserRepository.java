package com.example.gradox2.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.gradox2.persistence.entities.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findById(Long id);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    void deleteById(Long id);
}