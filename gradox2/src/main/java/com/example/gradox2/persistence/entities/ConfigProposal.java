package com.example.gradox2.persistence.entities;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@DiscriminatorValue("3")
public class ConfigProposal extends Proposal {

    @Column(name = "proposed_quorum_required")
    private Integer proposedQuorumRequired;

    @Column(name = "proposed_approval_threshold")
    private Double proposedApprovalThreshold;

    @Column(name = "proposed_max_pending_uploads")
    private Integer proposedMaxPendingUploads;

    @Column(name = "proposed_master_vote_weight")
    private Double proposedMasterVoteWeight;

    @Column(name = "proposed_user_vote_weight")
    private Double proposedUserVoteWeight;
}