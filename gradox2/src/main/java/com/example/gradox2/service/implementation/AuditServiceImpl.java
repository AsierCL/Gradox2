package com.example.gradox2.service.implementation;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.gradox2.persistence.entities.AuditRecord;
import com.example.gradox2.persistence.entities.User;
import com.example.gradox2.persistence.entities.enums.ActionType;
import com.example.gradox2.persistence.repository.AuditRecordRepository;
import com.example.gradox2.presentation.dto.admin.AuditLogResponse;
import com.example.gradox2.service.interfaces.IAuditService;
import com.example.gradox2.utils.GetAuthUser;

@Service
public class AuditServiceImpl implements IAuditService {

    private static final int MAX_PAGE_SIZE = 100;
    private static final Logger logger = LoggerFactory.getLogger(AuditServiceImpl.class);

    private final AuditRecordRepository auditRecordRepository;

    public AuditServiceImpl(AuditRecordRepository auditRecordRepository) {
        this.auditRecordRepository = auditRecordRepository;
    }

    @Override
    @Transactional
    public void record(ActionType actionType, String targetEntity, Long targetId, String details) {
        User actor = GetAuthUser.getCurrentUserOrNull();
        
        AuditRecord record = new AuditRecord();
        record.setActor(actor);  // Puede ser null para auditorías a nivel de sistema
        record.setActionType(actionType);
        record.setTargetEntity(targetEntity);
        record.setTargetId(targetId);
        record.setDetails(details);
        auditRecordRepository.save(record);

        // Loguear auditoría sin actor
        if (actor == null) {
            logger.warn("Auditoría registrada sin actor (sistema): {} - {} (id: {})",
                    actionType, targetEntity, targetId);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogResponse> getLogs(Long actorId, ActionType actionType, Instant from,
                                          Instant to, int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "timestamp"));

        Page<AuditRecord> records = auditRecordRepository.search(actorId, actionType, from, to, pageable);
        return records.map(AuditLogResponse::from);
    }
}