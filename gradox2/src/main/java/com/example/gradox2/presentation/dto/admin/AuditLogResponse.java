package com.example.gradox2.presentation.dto.admin;

import java.time.Instant;

import com.example.gradox2.persistence.entities.AuditRecord;
import com.example.gradox2.persistence.entities.enums.ActionType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Schema(description = "Entrada del registro de auditoría")
public class AuditLogResponse {

    @Schema(description = "ID del registro", example = "1")
    private Long id;

    @Schema(description = "Nombre del actor", example = "master1")
    private String actor;

    @Schema(description = "Tipo de acción", example = "BAN")
    private ActionType actionType;

    @Schema(description = "Entidad sobre la que se actuó", example = "User")
    private String targetEntity;

    @Schema(description = "ID de la entidad objetivo", example = "42")
    private Long targetId;

    @Schema(description = "Detalles de la acción", example = "Usuario baneado")
    private String details;

    @Schema(description = "Fecha de la acción")
    private Instant timestamp;

    public static AuditLogResponse from(AuditRecord record) {
        return AuditLogResponse.builder()
                .id(record.getId())
                .actor(record.getActor() != null ? record.getActor().getUsername() : "[SYSTEM]")
                .actionType(record.getActionType())
                .targetEntity(record.getTargetEntity())
                .targetId(record.getTargetId())
                .details(record.getDetails())
                .timestamp(record.getTimestamp())
                .build();
    }
}