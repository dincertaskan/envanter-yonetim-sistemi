package com.inventory.controller;

import com.inventory.dto.ReservationDtos.ReservationRequest;
import com.inventory.dto.ReservationDtos.ReservationResponse;
import com.inventory.security.UserPrincipal;
import com.inventory.service.ReservationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping
    public List<ReservationResponse> getAll() {
        return reservationService.getAll();
    }

    // Rezervasyonu yapan kişi token'dan alınır, istemciden gelmez
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationResponse create(@Valid @RequestBody ReservationRequest request,
                                      @AuthenticationPrincipal UserPrincipal principal) {
        return reservationService.create(request, principal.id());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancel(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        reservationService.cancel(id, principal);
    }
}
