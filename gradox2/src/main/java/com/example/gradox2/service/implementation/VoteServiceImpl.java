package com.example.gradox2.service.implementation;

import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.gradox2.persistence.entities.File;
import com.example.gradox2.persistence.entities.FileProposal;
import com.example.gradox2.persistence.entities.PromotionProposal;
import com.example.gradox2.persistence.entities.Proposal;
import com.example.gradox2.persistence.entities.TempFile;
import com.example.gradox2.persistence.entities.User;
import com.example.gradox2.persistence.entities.Vote;
import com.example.gradox2.persistence.entities.enums.ActionType;
import com.example.gradox2.persistence.entities.enums.ProposalStatus;
import com.example.gradox2.persistence.entities.enums.UserRole;
import com.example.gradox2.persistence.repository.*;
import com.example.gradox2.presentation.dto.vote.VoteResponse;
import com.example.gradox2.presentation.dto.vote.VoteResultResponse;
import com.example.gradox2.service.exceptions.AlreadyExistsException;
import com.example.gradox2.service.exceptions.InvalidFileOperation;
import com.example.gradox2.service.exceptions.NotFoundException;
import com.example.gradox2.service.exceptions.ProposalClosedException;
import com.example.gradox2.service.interfaces.IVoteService;
import com.example.gradox2.utils.GetAuthUser;
import com.example.gradox2.utils.mapper.VoteMapper;

@Service
@Transactional
public class VoteServiceImpl implements IVoteService {

    private final FileRepository fileRepository;
    private final UserRepository userRepository;
    private final VoteRepository voteRepository;
    private final ProposalRepository proposalRepository;
    private final FileProposalRepository fileProposalRepository;
    private final PromotionProposalRepository promotionProposalRepository;

    public VoteServiceImpl(VoteRepository voteRepository, ProposalRepository proposalRepository,
            FileProposalRepository fileProposalRepository,
            PromotionProposalRepository promotionProposalRepository, UserRepository userRepository,
            FileRepository fileRepository) {
        this.voteRepository = voteRepository;
        this.proposalRepository = proposalRepository;
        this.fileProposalRepository = fileProposalRepository;
        this.promotionProposalRepository = promotionProposalRepository;
        this.userRepository = userRepository;
        this.fileRepository = fileRepository;
    }

    @Override
    public VoteResponse voteProposal(Long proposalId, boolean upvote) {
        User auth = GetAuthUser.getAuthUser();
        Proposal proposal = proposalRepository.findById(proposalId)
                .orElseThrow(() -> new NotFoundException("Proposal not found"));

        if (proposal.getStatus() != ProposalStatus.PENDING) {
            throw new ProposalClosedException("This proposal is no longer open for voting.");
        }

        if (isExpired(proposal)) {
            rejectProposal(proposal);
            throw new ProposalClosedException("This proposal has expired.");
        }

        if (voteRepository.findByVoterAndProposal(auth, proposal).isPresent()) {
            throw new AlreadyExistsException("Already voted on this proposal.");
        }

        Vote vote = Vote.builder()
                .voter(auth)
                .proposal(proposal)
                .inFavor(upvote)
                .build();

        voteRepository.save(vote);
        checkProposalStatus(proposal);

        return VoteMapper.toVoteResponse(vote, auth);
    }

    @Override
    public String retractVote(Long proposalId) {
        User auth = GetAuthUser.getAuthUser();
        Proposal proposal = proposalRepository.findById(proposalId)
                .orElseThrow(() -> new NotFoundException("Proposal not found"));

        if (proposal.getStatus() != ProposalStatus.PENDING) {
            throw new ProposalClosedException("This proposal is no longer open for voting.");
        }

        if (isExpired(proposal)) {
            rejectProposal(proposal);
            throw new ProposalClosedException("This proposal has expired.");
        }

        Vote vote = voteRepository.findByVoterAndProposal(auth, proposal)
                .orElseThrow(() -> new NotFoundException("No vote found for this proposal."));

        voteRepository.delete(vote);
        checkProposalStatus(proposal);
        return "Vote retracted successfully.";
    }

    @Override
    public VoteResultResponse getVoteCount(Long id) {
        Proposal proposal = proposalRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Proposal not found"));

        long upvotes = voteRepository.countByProposalAndInFavor(proposal, true);
        long downvotes = voteRepository.countByProposalAndInFavor(proposal, false);

        return new VoteResultResponse(id, upvotes, downvotes);
    }

    @Override
    public VoteResponse getMyVote(Long id) {
        User auth = GetAuthUser.getAuthUser();
        Proposal proposal = proposalRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Proposal not found"));

        return voteRepository.findByVoterAndProposal(auth, proposal)
                .map(vote -> VoteMapper.toVoteResponse(vote, auth))
                .orElseThrow(() -> new NotFoundException("You have not voted on this proposal."));
    }

    // ──────────────────────────────────────────────
    // Internal helpers
    // ──────────────────────────────────────────────

    private boolean isExpired(Proposal proposal) {
        return proposal.getEndsAt() != null && Instant.now().isAfter(proposal.getEndsAt());
    }

    private void checkProposalStatus(Proposal proposal) {
        if (proposal.getStatus() != ProposalStatus.PENDING) {
            return;
        }

        long upvotes = voteRepository.countByProposalAndInFavor(proposal, true);
        long downvotes = voteRepository.countByProposalAndInFavor(proposal, false);
        int totalVotes = (int) (upvotes + downvotes);

        if (totalVotes < proposal.getQuorumRequired()) {
            return;
        }

        double approvalRatio = (double) upvotes / totalVotes;
        if (approvalRatio < proposal.getApprovalThreshold()) {
            rejectProposal(proposal);
        } else {
            applyProposal(proposal);
        }
    }

    private void closeProposal(Proposal proposal, ProposalStatus status) {
        proposal.setStatus(status);
        proposal.setClosedAt(Instant.now());
        proposalRepository.save(proposal);
    }

    private void rejectProposal(Proposal proposal) {
        closeProposal(proposal, ProposalStatus.REJECTED);
    }

    private void applyProposal(Proposal proposal) {
        if (proposal instanceof FileProposal fp) {
            applyFileProposal(fp);
        } else if (proposal instanceof PromotionProposal pp) {
            applyPromotionProposal(pp);
        }
        // If neither, the proposal was closed above — nothing else to do.
    }

    private void applyFileProposal(FileProposal fileProposal) {
        fileProposal.setStatus(ProposalStatus.APPROVED);
        fileProposal.setClosedAt(Instant.now());

        if (fileProposal.getActionType() == ActionType.DELETE) {
            if (fileProposal.getFile() != null) {
                File target = fileProposal.getFile();
                fileProposal.setFile(null);
                fileRepository.delete(target);
            }
            return;
        }

        TempFile tempFile = fileProposal.getTempFile();
        if (tempFile == null) {
            throw new InvalidFileOperation("No temp file associated with this upload proposal");
        }

        File file = File.builder()
                .title(tempFile.getTitle())
                .description(tempFile.getDescription())
                .type(tempFile.getType())
                .fileData(tempFile.getFileData())
                .fileHash(tempFile.getFileHash())
                .uploader(tempFile.getUploader())
                .subject(tempFile.getSubject())
                .visibilityLevel(tempFile.getVisibilityLevel())
                .build();

        fileProposal.setTempFile(null);
        fileProposal.setFile(file);
        fileRepository.save(file);
        fileProposalRepository.save(fileProposal);
    }

    private void applyPromotionProposal(PromotionProposal promotionProposal) {
        promotionProposal.setStatus(ProposalStatus.APPROVED);
        promotionProposal.setClosedAt(Instant.now());

        User candidate = promotionProposal.getCandidate();
        candidate.setRole(
                promotionProposal.getActionType() == ActionType.EXPULSION ? UserRole.USER : UserRole.MASTER);
        userRepository.save(candidate);
        promotionProposalRepository.save(promotionProposal);
    }
}
