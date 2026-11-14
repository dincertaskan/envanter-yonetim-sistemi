package com.inventory.service;

import com.inventory.dto.EquipmentDtos.EquipmentRequest;
import com.inventory.dto.EquipmentDtos.EquipmentResponse;
import com.inventory.exception.BusinessException;
import com.inventory.exception.NotFoundException;
import com.inventory.model.AuditAction;
import com.inventory.model.AuditLog;
import com.inventory.model.Equipment;
import com.inventory.model.FieldChange;
import com.inventory.model.User;
import com.inventory.repository.AuditLogRepository;
import com.inventory.repository.EquipmentRepository;
import com.inventory.repository.ReservationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class EquipmentService {

    private final EquipmentRepository equipmentRepository;
    private final ReservationRepository reservationRepository;
    private final AuditLogRepository auditLogRepository;
    private final UserService userService;

    public EquipmentService(EquipmentRepository equipmentRepository,
                            ReservationRepository reservationRepository,
                            AuditLogRepository auditLogRepository,
                            UserService userService) {
        this.equipmentRepository = equipmentRepository;
        this.reservationRepository = reservationRepository;
        this.auditLogRepository = auditLogRepository;
        this.userService = userService;
    }

    @Transactional(readOnly = true)
    public List<EquipmentResponse> getAllActive() {
        return equipmentRepository.findByActiveTrueOrderByNameAsc().stream()
                .map(EquipmentResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public EquipmentResponse getById(Long id) {
        return EquipmentResponse.from(findActive(id));
    }

    @Transactional(readOnly = true)
    public Equipment findActive(Long id) {
        return equipmentRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new NotFoundException("Ekipman bulunamadı"));
    }

    @Transactional
    public EquipmentResponse create(EquipmentRequest request, Long currentUserId) {
        if (equipmentRepository.existsBySerialNumberIgnoreCaseAndActiveTrue(request.serialNumber().trim())) {
            throw new BusinessException("Bu seri numarasına sahip bir ekipman zaten var", HttpStatus.CONFLICT);
        }

        User currentUser = userService.getById(currentUserId);
        Equipment equipment = new Equipment();
        applyRequest(equipment, request);
        equipment.setCreatedBy(currentUser);
        equipmentRepository.save(equipment);

        auditLogRepository.save(new AuditLog(AuditAction.CREATE, equipment, currentUser.getFullName(), List.of()));
        return EquipmentResponse.from(equipment);
    }

    @Transactional
    public EquipmentResponse update(Long id, EquipmentRequest request, Long currentUserId) {
        Equipment equipment = findActive(id);
        if (equipmentRepository.existsBySerialNumberIgnoreCaseAndActiveTrueAndIdNot(request.serialNumber().trim(), id)) {
            throw new BusinessException("Bu seri numarasına sahip başka bir ekipman var", HttpStatus.CONFLICT);
        }

        List<FieldChange> changes = diff(equipment, request);
        if (!changes.isEmpty()) {
            applyRequest(equipment, request);
            String performedBy = userService.getById(currentUserId).getFullName();
            auditLogRepository.save(new AuditLog(AuditAction.UPDATE, equipment, performedBy, changes));
        }
        return EquipmentResponse.from(equipment);
    }

    /**
     * Ekipmanı pasife alır (kayıt silinmez, raporlarda görünmeye devam eder)
     * ve henüz başlamamış rezervasyonlarını iptal eder.
     */
    @Transactional
    public void delete(Long id, Long currentUserId) {
        Equipment equipment = findActive(id);
        equipment.setActive(false);
        reservationRepository.deleteByEquipmentIdAndStartTimeAfter(id, LocalDateTime.now());

        String performedBy = userService.getById(currentUserId).getFullName();
        auditLogRepository.save(new AuditLog(AuditAction.DELETE, equipment, performedBy, List.of()));
    }

    private static void applyRequest(Equipment equipment, EquipmentRequest request) {
        equipment.setName(request.name().trim());
        equipment.setBrand(request.brand().trim());
        equipment.setModel(request.model().trim());
        equipment.setSerialNumber(request.serialNumber().trim());
        equipment.setCalibrationDate(request.calibrationDate());
        equipment.setDescription(normalize(request.description()));
    }

    /**
     * Mevcut ekipman ile güncelleme isteği arasındaki farkları bulur.
     */
    static List<FieldChange> diff(Equipment current, EquipmentRequest request) {
        List<FieldChange> changes = new ArrayList<>();
        addIfChanged(changes, "name", current.getName(), request.name().trim());
        addIfChanged(changes, "brand", current.getBrand(), request.brand().trim());
        addIfChanged(changes, "model", current.getModel(), request.model().trim());
        addIfChanged(changes, "serialNumber", current.getSerialNumber(), request.serialNumber().trim());
        addIfChanged(changes, "calibrationDate",
                Objects.toString(current.getCalibrationDate(), null),
                Objects.toString(request.calibrationDate(), null));
        addIfChanged(changes, "description", current.getDescription(), normalize(request.description()));
        return changes;
    }

    private static void addIfChanged(List<FieldChange> changes, String field, String oldValue, String newValue) {
        if (!Objects.equals(oldValue, newValue)) {
            changes.add(new FieldChange(field, oldValue, newValue));
        }
    }

    // Boş açıklama null olarak saklanır
    private static String normalize(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }
}
