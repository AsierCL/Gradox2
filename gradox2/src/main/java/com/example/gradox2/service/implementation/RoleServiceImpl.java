package com.example.gradox2.service.implementation;

import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.example.gradox2.persistence.entities.PromotionProposal;
import com.example.gradox2.persistence.entities.User;
import com.example.gradox2.persistence.entities.VoteConfig;
import com.example.gradox2.persistence.entities.enums.ActionType;
import com.example.gradox2.persistence.entities.enums.ProposalStatus;
import com.example.gradox2.persistence.entities.enums.UserRole;
import com.example.gradox2.persistence.repository.PromotionProposalRepository;
import com.example.gradox2.persistence.repository.UserRepository;
import com.example.gradox2.presentation.dto.promotionProposal.PromotionProposalResponse;
import com.example.gradox2.service.exceptions.InvalidRoleOperationException;
import com.example.gradox2.service.exceptions.NotFoundException;
import com.example.gradox2.service.interfaces.IRoleService;
import com.example.gradox2.service.interfaces.IVoteConfigService;
import com.example.gradox2.utils.GetAuthUser;
import com.example.gradox2.utils.SortUtils;
import com.example.gradox2.utils.mapper.PromotionProposerMapper;

import jakarta.transaction.Transactional;

@Service
public class RoleServiceImpl implements IRoleService{
    private static final int MAX_PAGE_SIZE = 100;
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "id",
            "quorumRequired",
            "approvalThreshold",
            "createdAt",
            "closedAt",
            "status",
            "actionType");

    private final UserRepository userRepository;
    private final PromotionProposalRepository promotionProposalRepository;
    private final IVoteConfigService voteConfigService;

    public RoleServiceImpl(UserRepository userRepository, PromotionProposalRepository promotionProposalRepository,
            IVoteConfigService voteConfigService) {
        this.userRepository = userRepository;
        this.promotionProposalRepository = promotionProposalRepository;
        this.voteConfigService = voteConfigService;
    }

    @Override
    public PromotionProposalResponse promoteToMaster() {
        User user = GetAuthUser.getAuthUser();

        if(user.getRole().equals(UserRole.MASTER)) {
            throw new InvalidRoleOperationException("El usuario ya es Master.");
        }

        VoteConfig config = voteConfigService.getConfig();
        PromotionProposal proposal = new PromotionProposal();
        proposal.setProposer(user);
        proposal.setStatus(ProposalStatus.PENDING);
        proposal.setActionType(ActionType.PROMOTION);
        proposal.setQuorumRequired(config.getQuorumRequired());
        proposal.setApprovalThreshold(config.getApprovalThreshold());
        proposal.setCandidate(user);

        promotionProposalRepository.save(proposal);

        return PromotionProposerMapper.toPromotionProposalResponse(proposal);
    }

    @Override
    public List<PromotionProposalResponse> getPendingPromoteProposals() {
        return getPendingPromoteProposalsPaged(0, MAX_PAGE_SIZE, "id").getContent();
    }

    @Override
    public Page<PromotionProposalResponse> getPendingPromoteProposalsPaged(int page, int size, String sortBy) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        String safeSortBy = SortUtils.resolveSortBy(sortBy, "id", ALLOWED_SORT_FIELDS);
        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(safeSortBy).descending());
        Page<PromotionProposal> proposalPage = promotionProposalRepository.findByStatus(ProposalStatus.PENDING, pageable);
        return proposalPage.map(PromotionProposerMapper::toPromotionProposalResponse);
    }

    @Override
    public PromotionProposalResponse demoteToUser(Long id) {
        User authUser = GetAuthUser.getAuthUser();
        User candidate = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (!candidate.getRole().equals(UserRole.MASTER)) {
            throw new InvalidRoleOperationException("El usuario no es Master.");
        }

        VoteConfig config = voteConfigService.getConfig();
        PromotionProposal proposal = new PromotionProposal();
        proposal.setProposer(authUser);
        proposal.setStatus(ProposalStatus.PENDING);
        proposal.setActionType(ActionType.EXPULSION);
        proposal.setQuorumRequired(config.getQuorumRequired());
        proposal.setApprovalThreshold(config.getApprovalThreshold());
        proposal.setCandidate(candidate);

        promotionProposalRepository.save(proposal);

        return PromotionProposerMapper.toPromotionProposalResponse(proposal);
    }

    @Override
    @Transactional
    public String deleteMyPromoteRequest() {
        promotionProposalRepository.deleteByProposer(GetAuthUser.getAuthUser());
        return "Promotion request deleted successfully";
    }

    @Override
    public PromotionProposalResponse getPromoteProposalById(Long id) {
        return PromotionProposerMapper.toPromotionProposalResponse(promotionProposalRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Proposal not found")));
    }

}
