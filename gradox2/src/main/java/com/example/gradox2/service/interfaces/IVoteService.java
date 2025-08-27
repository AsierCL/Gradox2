package com.example.gradox2.service.interfaces;

public interface IVoteService {
    String getVoteMyVote(Long id);
    String getVoteCount(Long id);
    String voteProposal(Long proposalId, boolean upvote);
    String retractVote(Long proposalId);


}
