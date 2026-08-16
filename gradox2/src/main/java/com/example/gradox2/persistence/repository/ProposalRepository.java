package com.example.gradox2.persistence.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

import com.example.gradox2.persistence.entities.Proposal;
import com.example.gradox2.persistence.entities.enums.ProposalStatus;

public interface ProposalRepository extends JpaRepository<Proposal, Long> {

    List<Proposal> findByStatusAndEndsAtBefore(ProposalStatus status, Instant now);
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Proposal p WHERE p.id = :id")
    Optional<Proposal> findByIdForUpdate(@Param("id") Long id);
}
