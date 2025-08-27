package com.example.gradox2.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.gradox2.persistence.entities.Proposal;

public interface ProposalRepository extends JpaRepository<Proposal, Long> {

}
