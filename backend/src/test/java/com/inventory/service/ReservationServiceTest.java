package com.inventory.service;

import com.inventory.dto.ReservationDtos.ReservationRequest;
import com.inventory.dto.ReservationDtos.ReservationResponse;
import com.inventory.exception.BusinessException;
import com.inventory.model.Equipment;
import com.inventory.model.Reservation;
import com.inventory.model.Role;
import com.inventory.model.User;
import com.inventory.repository.ReservationRepository;
import com.inventory.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ReservationServiceTest {

    // Testlerde "şu an" her zaman 2030-01-01 09:00
    private static final LocalDateTime NOW = LocalDateTime.of(2030, 1, 1, 9, 0);

    private ReservationRepository reservationRepository;
    private EquipmentService equipmentService;
    private UserService userService;
    private ReservationService reservationService;

    private Equipment equipment;
    private User user;

    @BeforeEach
    void setUp() {
        reservationRepository = mock(ReservationRepository.class);
        equipmentService = mock(EquipmentService.class);
        userService = mock(UserService.class);
        ZoneId zone = ZoneId.systemDefault();
        Clock clock = Clock.fixed(NOW.atZone(zone).toInstant(), zone);
        reservationService = new ReservationService(reservationRepository, equipmentService, userService, clock);

        equipment = new Equipment();
        equipment.setId(10L);
        equipment.setName("Osiloskop");
        equipment.setSerialNumber("SN-001");

        user = new User();
        user.setId(1L);
        user.setFirstName("Ali");
        user.setLastName("Yılmaz");
    }

    @Test
    void createsReservationWhenTimeSlotIsFree() {
        when(equipmentService.findActive(10L)).thenReturn(equipment);
        when(userService.getById(1L)).thenReturn(user);
        when(reservationRepository.existsOverlapping(any(), any(), any())).thenReturn(false);
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(inv -> inv.getArgument(0));

        ReservationResponse response = reservationService.create(
                new ReservationRequest(10L, NOW.plusHours(1), NOW.plusHours(3)), 1L);

        assertThat(response.equipmentName()).isEqualTo("Osiloskop");
        assertThat(response.userFullName()).isEqualTo("Ali Yılmaz");
        verify(reservationRepository).save(any(Reservation.class));
    }

    @Test
    void rejectsEndTimeBeforeStartTime() {
        ReservationRequest request = new ReservationRequest(10L, NOW.plusHours(3), NOW.plusHours(1));

        assertThatThrownBy(() -> reservationService.create(request, 1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Bitiş zamanı");
        verify(reservationRepository, never()).save(any());
    }

    @Test
    void rejectsReservationInThePast() {
        ReservationRequest request = new ReservationRequest(10L, NOW.minusHours(2), NOW.plusHours(1));

        assertThatThrownBy(() -> reservationService.create(request, 1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Geçmiş");
    }

    @Test
    void rejectsOverlappingReservation() {
        when(equipmentService.findActive(10L)).thenReturn(equipment);
        when(reservationRepository.existsOverlapping(eq(10L), any(), any())).thenReturn(true);
        ReservationRequest request = new ReservationRequest(10L, NOW.plusHours(1), NOW.plusHours(2));

        assertThatThrownBy(() -> reservationService.create(request, 1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("zaten rezerve");
        verify(reservationRepository, never()).save(any());
    }

    @Test
    void userCannotCancelSomeoneElsesReservation() {
        Reservation reservation = new Reservation(equipment, user, NOW.plusHours(1), NOW.plusHours(2));
        when(reservationRepository.findById(5L)).thenReturn(Optional.of(reservation));
        UserPrincipal otherUser = new UserPrincipal(2L, "veli@test.com", "Veli Demir", "x", Role.USER);

        assertThatThrownBy(() -> reservationService.cancel(5L, otherUser))
                .isInstanceOf(AccessDeniedException.class);
        verify(reservationRepository, never()).delete(any());
    }

    @Test
    void adminCanCancelAnyReservation() {
        Reservation reservation = new Reservation(equipment, user, NOW.plusHours(1), NOW.plusHours(2));
        when(reservationRepository.findById(5L)).thenReturn(Optional.of(reservation));
        UserPrincipal admin = new UserPrincipal(99L, "admin@test.com", "Admin", "x", Role.ADMIN);

        reservationService.cancel(5L, admin);

        verify(reservationRepository).delete(reservation);
    }
}
