package com.inventory.repository;

import com.inventory.model.AuditAction;
import com.inventory.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findAllByOrderByCreatedAtDesc();

    List<AuditLog> findByActionOrderByCreatedAtDesc(AuditAction action);
}
