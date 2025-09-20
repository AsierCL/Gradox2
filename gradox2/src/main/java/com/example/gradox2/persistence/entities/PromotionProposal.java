package com.example.gradox2.persistence.entities;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@DiscriminatorValue("PROMOTION")
public class PromotionProposal extends Proposal {
    @ManyToOne
    @JoinColumn(name = "candidate_id", nullable = false)
    private User candidate;
}
