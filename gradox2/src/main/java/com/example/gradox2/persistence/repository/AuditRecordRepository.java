package com.example.gradox2.persistence.repository;

import java.time.Instant;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.gradox2.persistence.entities.AuditRecord;
import com.example.gradox2.persistence.entities.enums.ActionType;

public interface AuditRecordRepository extends JpaRepository<AuditRecord, Long> {

    @Query("""
            SELECT a FROM AuditRecord a
            WHERE (:actorId IS NULL OR a.actor.id = :actorId)
              AND (:actionType IS NULL OR a.actionType = :actionType)
              AND (:from IS NULL OR a.timestamp >= :from)
              AND (:to IS NULL OR a.timestamp <= :to)
            """)
    Page<AuditRecord> search(@Param("actorId") Long actorId,
                             @Param("actionType") ActionType actionType,
                             @Param("from") Instant from,
                             @Param("to") Instant to,
                             Pageable pageable);
}
