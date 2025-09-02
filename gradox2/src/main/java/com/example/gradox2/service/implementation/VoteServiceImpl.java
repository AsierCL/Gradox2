package com.example.gradox2.service.implementation;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.gradox2.persistence.entities.File;
import com.example.gradox2.persistence.entities.FileProposal;
import com.example.gradox2.persistence.entities.Proposal;
import com.example.gradox2.persistence.entities.TempFile;
import com.example.gradox2.persistence.entities.User;
import com.example.gradox2.persistence.entities.Vote;
import com.example.gradox2.persistence.entities.enums.ProposalStatus;
import com.example.gradox2.persistence.repository.*;
import com.example.gradox2.service.interfaces.IVoteService;
import com.example.gradox2.utils.GetAuthUser;

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

    public String voteProposal(Long proposalId, boolean upvote) {
        User auth = GetAuthUser.getAuthUser();
        // Buscar a proposta
        Proposal proposal = proposalRepository.findById(proposalId)
                .orElseThrow(() -> new RuntimeException("Proposal not found"));

        if(proposal.getStatus() != ProposalStatus.PENDING){
            return "This proposal is no longer open for voting.";
        }

        // Checkear si xa votou
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

        checkProposalStatus(proposal);

        return "Vote registered successfully.";
    }

    private void checkProposalStatus(Proposal proposal) {
        //De momento, para testear, si hay un voto a favor, se aprueba
        long upvotes = voteRepository.countByProposalAndInFavor(proposal, true);
        long downvotes = voteRepository.countByProposalAndInFavor(proposal, false);

        if (upvotes >= 1) {
            if(proposal instanceof FileProposal){
                FileProposal fileProposal = (FileProposal) proposal;
                fileProposal.setStatus(ProposalStatus.APPROVED);
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
        }
    }

    public String retractVote(Long proposalId) {
        User auth = GetAuthUser.getAuthUser();
        Proposal proposal = proposalRepository.findById(proposalId)
                .orElseThrow(() -> new RuntimeException("Proposal not found"));

        if(proposal.getStatus() != ProposalStatus.PENDING){
            return "This proposal is no longer open for voting.";
        }

        Optional<Vote> vote = voteRepository.findByVoterAndProposal(auth, proposal);
        if (vote.isPresent()) {
            voteRepository.delete(vote.get());
            return "Vote retracted successfully.";
        }

        return "No vote found for this proposal.";
    }

    public String getVoteCount(Long id) {
        Proposal proposal = proposalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Proposal not found"));

        long upvotes = voteRepository.countByProposalAndInFavor(proposal, true);
        long downvotes = voteRepository.countByProposalAndInFavor(proposal, false);

        return "Upvotes: " + upvotes + ", Downvotes: " + downvotes;
    }

    public String getMyVote(Long id) {
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
