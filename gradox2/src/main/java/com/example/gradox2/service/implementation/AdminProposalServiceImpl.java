package com.example.gradox2.service.implementation;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.gradox2.persistence.entities.ConfigProposal;
import com.example.gradox2.persistence.entities.File;
import com.example.gradox2.persistence.entities.FileProposal;
import com.example.gradox2.persistence.entities.Proposal;
import com.example.gradox2.persistence.entities.PromotionProposal;
import com.example.gradox2.persistence.entities.TempFile;
import com.example.gradox2.persistence.repository.ProposalRepository;
import com.example.gradox2.presentation.dto.admin.AdminProposalResponse;
import com.example.gradox2.service.interfaces.IAdminProposalService;

@Service
public class AdminProposalServiceImpl implements IAdminProposalService {

    private static final int MAX_PAGE_SIZE = 100;

    private final ProposalRepository proposalRepository;

    public AdminProposalServiceImpl(ProposalRepository proposalRepository) {
        this.proposalRepository = proposalRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AdminProposalResponse> getAllProposals(int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by("createdAt").descending());
        return proposalRepository.findAll(pageable).map(this::toResponse);
    }

    private AdminProposalResponse toResponse(Proposal proposal) {
        AdminProposalResponse.AdminProposalResponseBuilder builder = AdminProposalResponse.builder()
                .id(proposal.getId())
                .proposer(proposal.getProposer() != null ? proposal.getProposer().getUsername() : null)
                .actionType(proposal.getActionType() != null ? proposal.getActionType().name() : null)
                .status(proposal.getStatus() != null ? proposal.getStatus().name() : null)
                .quorumRequired(proposal.getQuorumRequired())
                .approvalThreshold(proposal.getApprovalThreshold())
                .createdAt(proposal.getCreatedAt())
                .endsAt(proposal.getEndsAt())
                .closedAt(proposal.getClosedAt());

        if (proposal instanceof FileProposal fileProposal) {
            TempFile tempFile = fileProposal.getTempFile();
            File file = fileProposal.getFile();
            if (tempFile != null) {
                builder.title(tempFile.getTitle());
                if (tempFile.getSubject() != null) {
                    builder.subjectName(tempFile.getSubject().getName());
                }
            } else if (file != null) {
                builder.title(file.getTitle());
                if (file.getSubject() != null) {
                    builder.subjectName(file.getSubject().getName());
                }
            }
        } else if (proposal instanceof PromotionProposal promotionProposal) {
            if (promotionProposal.getCandidate() != null) {
                builder.candidate(promotionProposal.getCandidate().getUsername());
            }
        } else if (proposal instanceof ConfigProposal configProposal) {
            builder.title(configProposal.getProposedQuorumRequired() != null
                    ? "Quórum: " + configProposal.getProposedQuorumRequired()
                    : null);
        }

        return builder.build();
    }
}