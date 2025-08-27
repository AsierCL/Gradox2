package com.example.gradox2.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.gradox2.persistence.entities.Proposal;
import com.example.gradox2.persistence.entities.User;
import com.example.gradox2.persistence.entities.Vote;
import java.util.List;
import java.util.Optional;


public interface VoteRepository extends JpaRepository<Vote, Long> {
    List<Vote> findByProposalId(Long proposalId);
    List<Vote> findByVoterId(Long voterId);
    Optional<Vote> findByVoterAndProposal(User voter, Proposal proposal);
    long countByProposalAndInFavor(Proposal proposal, boolean b);

}
