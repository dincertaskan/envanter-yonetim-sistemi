package com.inventory.service;

import com.inventory.dto.AuditLogResponse;
import com.inventory.model.AuditAction;
import com.inventory.model.AuditLog;
import com.inventory.repository.AuditLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    /**
     * action verilirse sadece o işlem tipindeki kayıtları, verilmezse hepsini döner (en yeni önce).
     */
    @Transactional(readOnly = true)
    public List<AuditLogResponse> getLogs(AuditAction action) {
        List<AuditLog> logs = (action == null)
                ? auditLogRepository.findAllByOrderByCreatedAtDesc()
                : auditLogRepository.findByActionOrderByCreatedAtDesc(action);
        return logs.stream().map(AuditLogResponse::from).toList();
    }
}
