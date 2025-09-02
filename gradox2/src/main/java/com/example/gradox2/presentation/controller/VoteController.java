package com.example.gradox2.presentation.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.gradox2.service.interfaces.IVoteService;

import org.apache.catalina.connector.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;



@RestController
@RequestMapping("/vote")
public class VoteController {

    private final IVoteService voteService;

    public VoteController(IVoteService voteService) {
        this.voteService = voteService;
    }


    @GetMapping("/{id}")
    public ResponseEntity<String> getMyVoteProposal(@PathVariable Long id) {
        return ResponseEntity.ok(voteService.getMyVote(id));
    }


    @GetMapping("/{id}/results")
    public ResponseEntity<String> getAllVotesProposal(@PathVariable Long id) {
        return ResponseEntity.ok(voteService.getVoteCount(id));
    }


    @PostMapping("/{id}/{upvote}")
    public ResponseEntity<String> voteProposal(@PathVariable Long id, @PathVariable boolean upvote) {
        return ResponseEntity.ok(voteService.voteProposal(id, upvote));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> retractVote(@PathVariable Long id) {
        return ResponseEntity.ok(voteService.retractVote(id));
    }


}
