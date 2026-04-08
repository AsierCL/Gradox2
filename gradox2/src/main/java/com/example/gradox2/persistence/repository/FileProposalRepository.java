package com.example.gradox2.persistence.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.gradox2.persistence.entities.FileProposal;
import com.example.gradox2.persistence.entities.File;
import com.example.gradox2.persistence.entities.enums.ProposalStatus;

public interface FileProposalRepository extends JpaRepository<FileProposal, Long> {
	boolean existsByFileAndStatus(File file, ProposalStatus status);
	List<FileProposal> findAllByFile(File file);

}
