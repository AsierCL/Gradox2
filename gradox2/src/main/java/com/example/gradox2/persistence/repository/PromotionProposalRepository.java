package com.example.gradox2.persistence.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.gradox2.persistence.entities.PromotionProposal;
import com.example.gradox2.persistence.entities.User;
import com.example.gradox2.persistence.entities.enums.ProposalStatus;

public interface PromotionProposalRepository extends JpaRepository<PromotionProposal, Long> {

    List<PromotionProposal> findByStatus(ProposalStatus pending);

    Page<PromotionProposal> findByStatus(ProposalStatus status, Pageable pageable);

    void deleteByProposer(User authUser);

}
