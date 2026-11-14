package com.inventory.dto;

import com.inventory.model.Equipment;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalDateTime;

public final class EquipmentDtos {

    private EquipmentDtos() {
    }

    public record EquipmentRequest(
            @NotBlank(message = "Ekipman adı zorunludur")
            @Size(max = 100)
            String name,

            @NotBlank(message = "Marka zorunludur")
            @Size(max = 100)
            String brand,

            @NotBlank(message = "Model zorunludur")
            @Size(max = 100)
            String model,

            @NotBlank(message = "Seri numarası zorunludur")
            @Size(max = 100)
            String serialNumber,

            LocalDate calibrationDate,

            @Size(max = 500, message = "Açıklama en fazla 500 karakter olabilir")
            String description
    ) {
    }

    public record EquipmentResponse(
            Long id,
            String name,
            String brand,
            String model,
            String serialNumber,
            LocalDate calibrationDate,
            String description,
            LocalDateTime createdAt,
            String createdBy
    ) {
        public static EquipmentResponse from(Equipment equipment) {
            return new EquipmentResponse(
                    equipment.getId(),
                    equipment.getName(),
                    equipment.getBrand(),
                    equipment.getModel(),
                    equipment.getSerialNumber(),
                    equipment.getCalibrationDate(),
                    equipment.getDescription(),
                    equipment.getCreatedAt(),
                    equipment.getCreatedBy().getFullName()
            );
        }
    }
}
