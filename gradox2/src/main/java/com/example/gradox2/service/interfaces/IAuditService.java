package com.example.gradox2.service.interfaces;

import java.time.Instant;

import org.springframework.data.domain.Page;

import com.example.gradox2.persistence.entities.enums.ActionType;
import com.example.gradox2.presentation.dto.admin.AuditLogResponse;

public interface IAuditService {

    void record(ActionType actionType, String targetEntity, Long targetId, String details);

    Page<AuditLogResponse> getLogs(Long actorId, ActionType actionType, Instant from,
                                   Instant to, int page, int size);
}