package com.example.gradox2.service.implementation;

import java.time.Instant;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.gradox2.persistence.entities.File;
import com.example.gradox2.persistence.entities.FileProposal;
import com.example.gradox2.persistence.entities.PromotionProposal;
import com.example.gradox2.persistence.entities.Proposal;
import com.example.gradox2.persistence.entities.TempFile;
import com.example.gradox2.persistence.entities.User;
import com.example.gradox2.persistence.entities.Vote;
import com.example.gradox2.persistence.entities.enums.ProposalStatus;
import com.example.gradox2.persistence.entities.enums.UserRole;
import com.example.gradox2.persistence.repository.*;
import com.example.gradox2.presentation.dto.vote.VoteResponse;
import com.example.gradox2.presentation.dto.vote.VoteResultResponse;
import com.example.gradox2.service.exceptions.AlreadyExistsException;
import com.example.gradox2.service.exceptions.NotFoundException;
import com.example.gradox2.service.exceptions.ProposalClosedException;
import com.example.gradox2.service.interfaces.IVoteService;
import com.example.gradox2.utils.GetAuthUser;
import com.example.gradox2.utils.mapper.VoteMapper;

@Service
public class VoteServiceImpl implements IVoteService {

    private final FileRepository fileRepository;
    private final TempFileRepository tempFileRepository;
    private final UserRepository userRepository;
    private final VoteRepository voteRepository;
    private final ProposalRepository proposalRepository;
    private final FileProposalRepository fileProposalRepository;
    private final PromotionProposalRepository promotionProposalRepository;

    public VoteServiceImpl(VoteRepository voteRepository, ProposalRepository proposalRepository,
            FileProposalRepository fileProposalRepository, TempFileRepository tempFileRepository,
            PromotionProposalRepository promotionProposalRepository, UserRepository userRepository, FileRepository fileRepository) {
        this.voteRepository = voteRepository;
        this.proposalRepository = proposalRepository;
        this.fileProposalRepository = fileProposalRepository;
        this.promotionProposalRepository = promotionProposalRepository;
        this.userRepository = userRepository;
        this.fileRepository = fileRepository;
        this.tempFileRepository = tempFileRepository;
    }

    public VoteResponse voteProposal(Long proposalId, boolean upvote) {
        User auth = GetAuthUser.getAuthUser();
        // Buscar a proposta
        Proposal proposal = proposalRepository.findById(proposalId)
                .orElseThrow(() -> new NotFoundException("Proposal not found"));

        if(proposal.getStatus() != ProposalStatus.PENDING){
            throw new ProposalClosedException("This proposal is no longer open for voting.");
        }

        // Checkear si xa votou
        Optional<Vote> existingVote = voteRepository.findByVoterAndProposal(auth, proposal);
        if (existingVote.isPresent()) {
            throw new AlreadyExistsException("Already voted on this proposal.");
        }



        Vote vote = Vote.builder()
                .voter(auth)
                .proposal(proposal)
                .inFavor(upvote)
                .build();

        voteRepository.save(vote);

        checkProposalStatus(proposal);

        return VoteMapper.toVoteResponse(vote);
    }

    private void checkProposalStatus(Proposal proposal) {
        //De momento, para testear, si hay un voto a favor, se aprueba
        long upvotes = voteRepository.countByProposalAndInFavor(proposal, true);
        long downvotes = voteRepository.countByProposalAndInFavor(proposal, false);

        if (upvotes >= 1) {
            if(proposal instanceof FileProposal){
                FileProposal fileProposal = (FileProposal) proposal;
                fileProposal.setStatus(ProposalStatus.APPROVED);
                fileProposal.setClosedAt(Instant.now());
                TempFile tempFile = fileProposal.getTempFile();
                if (tempFile != null) {
                    File file = File.builder()
                            .title(tempFile.getTitle())
                            .description(tempFile.getDescription())
                            .type(tempFile.getType())
                            .fileData(tempFile.getFileData())
                            .fileHash(tempFile.getFileHash())
                            .uploader(tempFile.getUploader())
                            .subject(tempFile.getSubject())
                            .build();

                            fileProposal.setTempFile(null);
                            fileProposal.setFile(file);
                            fileRepository.save(file);
                            tempFileRepository.delete(tempFile);
                            fileProposalRepository.save(fileProposal);
               }
            }
            if(proposal instanceof PromotionProposal){
                PromotionProposal promotionProposal = (PromotionProposal) proposal;
                promotionProposal.setStatus(ProposalStatus.APPROVED);
                promotionProposal.setClosedAt(Instant.now());
                User user = promotionProposal.getCandidate();
                user.setRole(UserRole.MASTER);
                userRepository.save(user);
                promotionProposalRepository.save(promotionProposal);
            }
        }
    }

    public String retractVote(Long proposalId) {
        User auth = GetAuthUser.getAuthUser();
        Proposal proposal = proposalRepository.findById(proposalId)
                .orElseThrow(() -> new NotFoundException("Proposal not found"));

        if(proposal.getStatus() != ProposalStatus.PENDING){
            throw new ProposalClosedException("This proposal is no longer open for voting.");
        }

        Optional<Vote> vote = voteRepository.findByVoterAndProposal(auth, proposal);
        if (!vote.isPresent()) {
            throw new NotFoundException("No vote found for this proposal.");
        }

        voteRepository.delete(vote.get());
        return "Vote retracted successfully.";
    }

    public VoteResultResponse getVoteCount(Long id) {
        Proposal proposal = proposalRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Proposal not found"));

        long upvotes = voteRepository.countByProposalAndInFavor(proposal, true);
        long downvotes = voteRepository.countByProposalAndInFavor(proposal, false);

        return new VoteResultResponse(upvotes, downvotes);
    }

    public VoteResponse getMyVote(Long id) {
        User auth = GetAuthUser.getAuthUser();
        Proposal proposal = proposalRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Proposal not found"));

        Optional<Vote> vote = voteRepository.findByVoterAndProposal(auth, proposal);
        if (vote.isPresent()) {
            return VoteMapper.toVoteResponse(vote.get());
        }

        throw new NotFoundException("You have not voted on this proposal.");
    }
}
