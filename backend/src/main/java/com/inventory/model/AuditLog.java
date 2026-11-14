package com.inventory.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Ekipmanlar üzerinde yapılan ekleme / güncelleme / silme işlemlerinin kaydı.
 */
@Entity
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AuditAction action;

    @Column(nullable = false)
    private Long equipmentId;

    @Column(nullable = false, length = 100)
    private String equipmentName;

    @Column(nullable = false, length = 101)
    private String performedBy;

    @ElementCollection
    @CollectionTable(name = "audit_log_changes", joinColumns = @JoinColumn(name = "audit_log_id"))
    private List<FieldChange> changes = new ArrayList<>();

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected AuditLog() {
    }

    public AuditLog(AuditAction action, Equipment equipment, String performedBy, List<FieldChange> changes) {
        this.action = action;
        this.equipmentId = equipment.getId();
        this.equipmentName = equipment.getName();
        this.performedBy = performedBy;
        this.changes = new ArrayList<>(changes);
    }

    public Long getId() { return id; }
    public AuditAction getAction() { return action; }
    public Long getEquipmentId() { return equipmentId; }
    public String getEquipmentName() { return equipmentName; }
    public String getPerformedBy() { return performedBy; }
    public List<FieldChange> getChanges() { return changes; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
