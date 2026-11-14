package com.inventory.dto;

import com.inventory.model.AuditAction;
import com.inventory.model.AuditLog;

import java.time.LocalDateTime;
import java.util.List;

public record AuditLogResponse(
        Long id,
        AuditAction action,
        Long equipmentId,
        String equipmentName,
        String performedBy,
        LocalDateTime createdAt,
        List<Change> changes
) {
    public record Change(String field, String oldValue, String newValue) {
    }

    public static AuditLogResponse from(AuditLog log) {
        List<Change> changes = log.getChanges().stream()
                .map(c -> new Change(c.getField(), c.getOldValue(), c.getNewValue()))
                .toList();
        return new AuditLogResponse(
                log.getId(),
                log.getAction(),
                log.getEquipmentId(),
                log.getEquipmentName(),
                log.getPerformedBy(),
                log.getCreatedAt(),
                changes
        );
    }
}
