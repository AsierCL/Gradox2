package com.example.gradox2.service.implementation;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.gradox2.persistence.entities.ConfigProposal;
import com.example.gradox2.persistence.entities.GlobalConfig;
import com.example.gradox2.persistence.entities.User;
import com.example.gradox2.persistence.entities.enums.ActionType;
import com.example.gradox2.persistence.entities.enums.ProposalStatus;
import com.example.gradox2.persistence.repository.ConfigProposalRepository;
import com.example.gradox2.presentation.dto.voteConfig.ConfigProposalResponse;
import com.example.gradox2.service.exceptions.InvalidRoleOperationException;
import com.example.gradox2.service.interfaces.IAuditService;
import com.example.gradox2.service.interfaces.IConfigProposalService;
import com.example.gradox2.service.interfaces.IGlobalConfigService;
import com.example.gradox2.utils.GetAuthUser;

@Service
@Transactional
public class ConfigProposalServiceImpl implements IConfigProposalService {

    private final ConfigProposalRepository configProposalRepository;
    private final IGlobalConfigService globalConfigService;
    private final IAuditService auditService;

    public ConfigProposalServiceImpl(ConfigProposalRepository configProposalRepository,
            IGlobalConfigService globalConfigService, IAuditService auditService) {
        this.configProposalRepository = configProposalRepository;
        this.globalConfigService = globalConfigService;
        this.auditService = auditService;
    }

    @Override
    @Transactional
    public ConfigProposalResponse createConfigProposal(Integer quorumRequired, Double approvalThreshold,
            Integer maxPendingUploads, Double masterVoteWeight, Double userVoteWeight) {
        User proposer = GetAuthUser.getAuthUser();

        if (proposer.getRole() != com.example.gradox2.persistence.entities.enums.UserRole.MASTER) {
            throw new InvalidRoleOperationException("Solo los usuarios MASTER pueden proponer cambios de política.");
        }

        // Validar parámetros solo si se proporcionan
        if (quorumRequired != null && quorumRequired <= 0) {
            throw new IllegalArgumentException("El quórum requerido debe ser mayor que 0.");
        }
        if (approvalThreshold != null && (approvalThreshold <= 0 || approvalThreshold > 1)) {
            throw new IllegalArgumentException("El umbral de aprobación debe estar entre 0 y 1.");
        }
        if (maxPendingUploads != null && maxPendingUploads <= 0) {
            throw new IllegalArgumentException("El máximo de cargas pendientes debe ser mayor que 0.");
        }
        if (masterVoteWeight != null && masterVoteWeight <= 0) {
            throw new IllegalArgumentException("El peso del voto MASTER debe ser mayor que 0.");
        }
        if (userVoteWeight != null && userVoteWeight <= 0) {
            throw new IllegalArgumentException("El peso del voto de usuario debe ser mayor que 0.");
        }

        GlobalConfig config = globalConfigService.getConfig();
        ConfigProposal proposal = new ConfigProposal();
        proposal.setProposer(proposer);
        proposal.setStatus(ProposalStatus.PENDING);
        proposal.setActionType(ActionType.POLICY_CHANGE);
        proposal.setQuorumRequired(config.getQuorumRequired());
        proposal.setApprovalThreshold(config.getApprovalThreshold());
        proposal.setProposedQuorumRequired(quorumRequired);
        proposal.setProposedApprovalThreshold(approvalThreshold);
        proposal.setProposedMaxPendingUploads(maxPendingUploads);
        proposal.setProposedMasterVoteWeight(masterVoteWeight);
        proposal.setProposedUserVoteWeight(userVoteWeight);

        configProposalRepository.save(proposal);

        auditService.record(ActionType.POLICY_CHANGE, "ConfigProposal", proposal.getId(),
                "Propuesta de cambio de configuración creada");

        return ConfigProposalResponse.builder()
                .id(proposal.getId())
                .proposer(proposer.getUsername())
                .status(proposal.getStatus().name())
                .quorumRequired(proposal.getProposedQuorumRequired())
                .approvalThreshold(proposal.getProposedApprovalThreshold())
                .maxPendingUploads(proposal.getProposedMaxPendingUploads())
                .masterVoteWeight(proposal.getProposedMasterVoteWeight())
                .userVoteWeight(proposal.getProposedUserVoteWeight())
                .createdAt(proposal.getCreatedAt())
                .endsAt(proposal.getEndsAt())
                .build();
    }
}