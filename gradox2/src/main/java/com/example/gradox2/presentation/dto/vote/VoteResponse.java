package com.example.gradox2.presentation.dto.vote;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class VoteResponse {
    public Long idVote;
    public Long proposalId;
    public String voter;
    public Boolean inFavor;
    public Instant votedAt;
}
