package com.example.gradox2.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.gradox2.persistence.entities.FileProposal;

public interface FileProposalRepository extends JpaRepository<FileProposal, Long> {

}
