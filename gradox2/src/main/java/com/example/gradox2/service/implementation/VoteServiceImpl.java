package com.example.gradox2.service.implementation;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.gradox2.persistence.entities.Proposal;
import com.example.gradox2.persistence.entities.User;
import com.example.gradox2.persistence.entities.Vote;
import com.example.gradox2.persistence.repository.FileProposalRepository;
import com.example.gradox2.persistence.repository.PromotionProposalRepository;
import com.example.gradox2.persistence.repository.ProposalRepository;
import com.example.gradox2.persistence.repository.VoteRepository;
import com.example.gradox2.service.interfaces.IVoteService;
import com.example.gradox2.utils.GetAuthUser;

@Service
public class VoteServiceImpl implements IVoteService {
    private final VoteRepository voteRepository;
    private final ProposalRepository proposalRepository;
    private final FileProposalRepository fileProposalRepository;
    private final PromotionProposalRepository promotionProposalRepository;

    public VoteServiceImpl(VoteRepository voteRepository, ProposalRepository proposalRepository,
            FileProposalRepository fileProposalRepository,
            PromotionProposalRepository promotionProposalRepository) {
        this.voteRepository = voteRepository;
        this.proposalRepository = proposalRepository;
        this.fileProposalRepository = fileProposalRepository;
        this.promotionProposalRepository = promotionProposalRepository;
    }

    public String voteProposal(Long proposalId, boolean upvote) {
        User auth = GetAuthUser.getAuthUser();
        // Buscar la propuesta (sea del tipo que sea)
        Proposal proposal = proposalRepository.findById(proposalId)
                .orElseThrow(() -> new RuntimeException("Proposal not found"));

        // Comprobar si el usuario ya votó
        Optional<Vote> existingVote = voteRepository.findByVoterAndProposal(auth, proposal);
        if (existingVote.isPresent()) {
            return "You have already voted for this proposal.";
        }

        Vote vote = Vote.builder()
                .voter(auth)
                .proposal(proposal)
                .inFavor(upvote)
                .build();

        voteRepository.save(vote);

        return "Vote registered successfully.";
    }

    public String retractVote(Long proposalId) {
        User auth = GetAuthUser.getAuthUser();
        Proposal proposal = proposalRepository.findById(proposalId)
                .orElseThrow(() -> new RuntimeException("Proposal not found"));

        Optional<Vote> vote = voteRepository.findByVoterAndProposal(auth, proposal);
        if (vote.isPresent()) {
            voteRepository.delete(vote.get());
            return "Vote retracted successfully.";
        }

        return "No vote found for this proposal.";
    }

    @Override
    public String getVoteCount(Long id) {
        Proposal proposal = proposalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Proposal not found"));

        long upvotes = voteRepository.countByProposalAndInFavor(proposal, true);
        long downvotes = voteRepository.countByProposalAndInFavor(proposal, false);

        return "Upvotes: " + upvotes + ", Downvotes: " + downvotes;
    }

    @Override
    public String getVoteMyVote(Long id) {
        User auth = GetAuthUser.getAuthUser();
        Proposal proposal = proposalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Proposal not found"));

        Optional<Vote> vote = voteRepository.findByVoterAndProposal(auth, proposal);
        if (vote.isPresent()) {
            return vote.get().getInFavor() ? "You voted in favor." : "You voted against.";
        }

        return "You have not voted on this proposal.";
    }

}
