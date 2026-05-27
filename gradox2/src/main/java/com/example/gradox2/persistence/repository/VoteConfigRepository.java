package com.example.gradox2.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

import com.example.gradox2.persistence.entities.GlobalConfig;

public interface VoteConfigRepository extends JpaRepository<GlobalConfig, Long> {
	Optional<GlobalConfig> findFirstByOrderByIdAsc();
}
