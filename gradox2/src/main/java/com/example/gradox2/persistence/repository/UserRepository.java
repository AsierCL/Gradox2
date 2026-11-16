package com.example.gradox2.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.gradox2.persistence.entities.Degree;
import com.example.gradox2.persistence.entities.User;
import com.example.gradox2.persistence.entities.enums.UserRole;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findById(Long id);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    void deleteById(Long id);
    long countByRole(UserRole role);
    long countByRoleAndEnabled(UserRole role, boolean enabled);

    /**
     * Masters activos de un grado. es la base de la gobernanza por grado (D3): el «último master» es el
     * último de SU grado, no el último de la plataforma.
     */
    long countByRoleAndEnabledAndDegree(UserRole role, boolean enabled, Degree degree);
    Page<User> findByEnabledFalse(Pageable pageable);
}
