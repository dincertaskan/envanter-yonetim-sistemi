package com.inventory.dto;

import com.inventory.model.Reservation;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public final class ReservationDtos {

    private ReservationDtos() {
    }

    public record ReservationRequest(
            @NotNull(message = "Ekipman seçilmelidir")
            Long equipmentId,

            @NotNull(message = "Başlangıç zamanı zorunludur")
            LocalDateTime startTime,

            @NotNull(message = "Bitiş zamanı zorunludur")
            LocalDateTime endTime
    ) {
    }

    public record ReservationResponse(
            Long id,
            Long equipmentId,
            String equipmentName,
            String serialNumber,
            Long userId,
            String userFullName,
            LocalDateTime startTime,
            LocalDateTime endTime
    ) {
        public static ReservationResponse from(Reservation reservation) {
            return new ReservationResponse(
                    reservation.getId(),
                    reservation.getEquipment().getId(),
                    reservation.getEquipment().getName(),
                    reservation.getEquipment().getSerialNumber(),
                    reservation.getUser().getId(),
                    reservation.getUser().getFullName(),
                    reservation.getStartTime(),
                    reservation.getEndTime()
            );
        }
    }
}
