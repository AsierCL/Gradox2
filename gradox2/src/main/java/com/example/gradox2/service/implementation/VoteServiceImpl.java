package com.example.gradox2.service.implementation;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.gradox2.persistence.entities.File;
import com.example.gradox2.persistence.entities.FileProposal;
import com.example.gradox2.persistence.entities.GlobalConfig;
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
import com.example.gradox2.service.interfaces.IGlobalConfigService;
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
    private final ForumThreadRepository forumThreadRepository;
    private final IGlobalConfigService globalConfigService;
    private final S3StorageService s3StorageService;

    public VoteServiceImpl(VoteRepository voteRepository, ProposalRepository proposalRepository,
            FileProposalRepository fileProposalRepository,
            PromotionProposalRepository promotionProposalRepository, UserRepository userRepository,
            FileRepository fileRepository, ForumThreadRepository forumThreadRepository,
            IGlobalConfigService globalConfigService,
            S3StorageService s3StorageService) {
        this.voteRepository = voteRepository;
        this.proposalRepository = proposalRepository;
        this.fileProposalRepository = fileProposalRepository;
        this.promotionProposalRepository = promotionProposalRepository;
        this.userRepository = userRepository;
        this.fileRepository = fileRepository;
        this.forumThreadRepository = forumThreadRepository;
        this.globalConfigService = globalConfigService;
        this.s3StorageService = s3StorageService;
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
        addReputation(auth, 1.0);
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

    @Override
    public int closeExpiredProposals() {
        List<Proposal> expired = proposalRepository.findByStatusAndEndsAtBefore(
                ProposalStatus.PENDING, Instant.now());

        for (Proposal proposal : expired) {
            rejectProposal(proposal);
        }

        return expired.size();
    }

    // ──────────────────────────────────────────────
    // Internal helpers
    // ──────────────────────────────────────────────

    private void addReputation(User user, double amount) {
        double newRep = Math.max(0, user.getReputation() + amount);
        user.setReputation(newRep);
        userRepository.save(user);
    }

    private boolean isExpired(Proposal proposal) {
        return proposal.getEndsAt() != null && Instant.now().isAfter(proposal.getEndsAt());
    }

    private void checkProposalStatus(Proposal proposal) {
        if (proposal.getStatus() != ProposalStatus.PENDING) {
            return;
        }

        List<Vote> votes = voteRepository.findByProposalId(proposal.getId());
        GlobalConfig config = globalConfigService.getConfig();

        double weightedInFavor = 0;
        double weightedTotal = 0;

        for (Vote vote : votes) {
            double weight = vote.getVoter().getRole() == UserRole.MASTER
                    ? config.getMasterVoteWeight()
                    : config.getUserVoteWeight();
            if (Boolean.TRUE.equals(vote.getInFavor())) {
                weightedInFavor += weight;
            }
            weightedTotal += weight;
        }

        if (votes.size() < proposal.getQuorumRequired()) {
            return;
        }

        double approvalRatio = weightedTotal > 0 ? weightedInFavor / weightedTotal : 0;
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
        if (proposal instanceof FileProposal fp && fp.getTempFile() != null) {
            addReputation(fp.getTempFile().getUploader(), -5.0);
        }
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
                s3StorageService.delete(target.getObjectKey());
                forumThreadRepository.deleteByFileId(target.getId());
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
                .objectKey(tempFile.getObjectKey())
                .fileHash(tempFile.getFileHash())
                .sizeBytes(tempFile.getSizeBytes())
                .uploader(tempFile.getUploader())
                .subject(tempFile.getSubject())
                .visibilityLevel(tempFile.getVisibilityLevel())
                .build();

        fileProposal.setTempFile(null);
        fileProposal.setFile(file);
        fileRepository.save(file);
        fileProposalRepository.save(fileProposal);

        addReputation(tempFile.getUploader(), 10.0);
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
