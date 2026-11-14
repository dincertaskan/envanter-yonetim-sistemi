package com.inventory.service;

import com.inventory.dto.ReservationDtos.ReservationRequest;
import com.inventory.dto.ReservationDtos.ReservationResponse;
import com.inventory.exception.BusinessException;
import com.inventory.exception.NotFoundException;
import com.inventory.model.Equipment;
import com.inventory.model.Reservation;
import com.inventory.model.User;
import com.inventory.repository.ReservationRepository;
import com.inventory.security.UserPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final EquipmentService equipmentService;
    private final UserService userService;
    private final Clock clock;

    public ReservationService(ReservationRepository reservationRepository,
                              EquipmentService equipmentService,
                              UserService userService,
                              Clock clock) {
        this.reservationRepository = reservationRepository;
        this.equipmentService = equipmentService;
        this.userService = userService;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> getAll() {
        return reservationRepository.findAllByOrderByStartTimeAsc().stream()
                .map(ReservationResponse::from)
                .toList();
    }

    @Transactional
    public ReservationResponse create(ReservationRequest request, Long currentUserId) {
        LocalDateTime start = request.startTime();
        LocalDateTime end = request.endTime();

        if (!start.isBefore(end)) {
            throw new BusinessException("Bitiş zamanı başlangıç zamanından sonra olmalıdır");
        }
        if (start.isBefore(LocalDateTime.now(clock))) {
            throw new BusinessException("Geçmiş bir zamana rezervasyon yapılamaz");
        }

        Equipment equipment = equipmentService.findActive(request.equipmentId());
        if (reservationRepository.existsOverlapping(equipment.getId(), start, end)) {
            throw new BusinessException("Ekipman bu zaman aralığında zaten rezerve edilmiş", HttpStatus.CONFLICT);
        }

        User user = userService.getById(currentUserId);
        Reservation saved = reservationRepository.save(new Reservation(equipment, user, start, end));
        return ReservationResponse.from(saved);
    }

    /**
     * Rezervasyonu iptal eder. Kullanıcı sadece kendi rezervasyonunu, yönetici ise hepsini iptal edebilir.
     */
    @Transactional
    public void cancel(Long reservationId, UserPrincipal currentUser) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new NotFoundException("Rezervasyon bulunamadı"));

        boolean isOwner = reservation.getUser().getId().equals(currentUser.id());
        if (!isOwner && !currentUser.isAdmin()) {
            throw new AccessDeniedException("Bu rezervasyonu iptal etme yetkiniz yok");
        }
        reservationRepository.delete(reservation);
    }
}
