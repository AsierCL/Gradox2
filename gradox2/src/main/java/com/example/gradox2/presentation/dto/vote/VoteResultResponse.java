package com.example.gradox2.presentation.dto.vote;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class VoteResultResponse {
    private Long upvotes;
    private Long downvotes;
}
