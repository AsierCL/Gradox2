package com.example.gradox2.presentation.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;

import com.example.gradox2.presentation.dto.vote.VoteResponse;
import com.example.gradox2.presentation.dto.vote.VoteResultResponse;
import com.example.gradox2.service.interfaces.IVoteService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import jakarta.validation.constraints.Positive;



@RestController
@RequestMapping("/vote")
@Validated
public class VoteController {

    private final IVoteService voteService;

    public VoteController(IVoteService voteService) {
        this.voteService = voteService;
    }


    @GetMapping("/{id}")
    public ResponseEntity<VoteResponse> getMyVoteProposal(@PathVariable @Positive Long id) {
        return ResponseEntity.ok(voteService.getMyVote(id));
    }


    @GetMapping("/{id}/results")
    public ResponseEntity<VoteResultResponse> getAllVotesProposal(@PathVariable @Positive Long id) {
        return ResponseEntity.ok(voteService.getVoteCount(id));
    }


    @PostMapping("/{id}/{upvote}")
    public ResponseEntity<VoteResponse> voteProposal(@PathVariable @Positive Long id, @PathVariable boolean upvote) {
        return ResponseEntity.ok(voteService.voteProposal(id, upvote));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> retractVote(@PathVariable @Positive Long id) {
        return ResponseEntity.ok(voteService.retractVote(id));
    }


}
