package com.example.gradox2.persistence.repository;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.gradox2.persistence.entities.Proposal;
import com.example.gradox2.persistence.entities.enums.ProposalStatus;

public interface ProposalRepository extends JpaRepository<Proposal, Long> {

    List<Proposal> findByStatusAndEndsAtBefore(ProposalStatus status, Instant now);
}
