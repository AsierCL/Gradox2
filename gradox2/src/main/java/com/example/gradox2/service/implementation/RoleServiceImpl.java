package com.example.gradox2.service.implementation;

import java.util.List;

import org.hibernate.usertype.UserType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.example.gradox2.persistence.entities.PromotionProposal;
import com.example.gradox2.persistence.entities.User;
import com.example.gradox2.persistence.entities.enums.ProposalStatus;
import com.example.gradox2.persistence.entities.enums.UserRole;
import com.example.gradox2.persistence.repository.PromotionProposalRepository;
import com.example.gradox2.persistence.repository.UserRepository;
import com.example.gradox2.presentation.dto.promotionProposal.PromotionProposalResponse;
import com.example.gradox2.service.exceptions.InvalidRoleOperationException;
import com.example.gradox2.service.exceptions.NotFoundException;
import com.example.gradox2.service.interfaces.IRoleService;
import com.example.gradox2.utils.GetAuthUser;
import com.example.gradox2.utils.mapper.PromotionProposerMapper;

import jakarta.transaction.Transactional;

@Service
public class RoleServiceImpl implements IRoleService{

    private final UserRepository userRepository;
    private final PromotionProposalRepository promotionProposalRepository;

    public RoleServiceImpl(UserRepository userRepository, PromotionProposalRepository promotionProposalRepository) {
        this.userRepository = userRepository;
        this.promotionProposalRepository = promotionProposalRepository;
    }

    @Override
    public PromotionProposalResponse promoteToMaster() {
        User user = GetAuthUser.getAuthUser();

        if(user.getRole().equals(UserRole.MASTER)) {
            throw new InvalidRoleOperationException("El usuario ya es Master.");
        }

        // TODO : FACER ESTO CON BUILDER E NON HARDCODEAR OS VALORES
        PromotionProposal proposal = new PromotionProposal();
        proposal.setProposer(user);
        proposal.setStatus(ProposalStatus.PENDING);
        proposal.setQuorumRequired(5);
        proposal.setApprovalThreshold(0.6);
        proposal.setCandidate(user);

        promotionProposalRepository.save(proposal);

        return PromotionProposerMapper.toPromotionProposalResponse(proposal);
    }

    @Override
    public List<PromotionProposalResponse> getPendingPromoteProposals() {
        return getPendingPromoteProposalsPaged(0, Integer.MAX_VALUE, "id").getContent();
    }

    @Override
    public Page<PromotionProposalResponse> getPendingPromoteProposalsPaged(int page, int size, String sortBy) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).descending());
        Page<PromotionProposal> proposalPage = promotionProposalRepository.findByStatus(ProposalStatus.PENDING, pageable);
        return proposalPage.map(PromotionProposerMapper::toPromotionProposalResponse);
    }

    @Override
    public PromotionProposalResponse demoteToUser(Long id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'demoteToUser'");
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
