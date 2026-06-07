package com.creditrisk.admin;

import com.creditrisk.audit.AuditLogEntity;

import java.time.Instant;

public record AuditLogResponse(
        Long id,
        Long actorUserId,
        String actionType,
        String entityType,
        String entityId,
        String ipAddress,
        Instant createdAt
) {
    public static AuditLogResponse from(AuditLogEntity e) {
        return new AuditLogResponse(e.getId(), e.getActorUser() == null ? null : e.getActorUser().getId(), e.getActionType(), e.getEntityType(), e.getEntityId(), e.getIpAddress(), e.getCreatedAt());
    }
}
