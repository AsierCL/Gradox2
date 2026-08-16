package com.example.gradox2.service.interfaces;

import org.springframework.data.domain.Page;

import com.example.gradox2.presentation.dto.admin.AdminProposalResponse;

public interface IAdminProposalService {
    Page<AdminProposalResponse> getAllProposals(int page, int size);
}