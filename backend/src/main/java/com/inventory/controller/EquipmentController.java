package com.inventory.controller;

import com.inventory.dto.EquipmentDtos.EquipmentRequest;
import com.inventory.dto.EquipmentDtos.EquipmentResponse;
import com.inventory.security.UserPrincipal;
import com.inventory.service.EquipmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/equipment")
public class EquipmentController {

    private final EquipmentService equipmentService;

    public EquipmentController(EquipmentService equipmentService) {
        this.equipmentService = equipmentService;
    }

    @GetMapping
    public List<EquipmentResponse> getAll() {
        return equipmentService.getAllActive();
    }

    @GetMapping("/{id}")
    public EquipmentResponse getById(@PathVariable Long id) {
        return equipmentService.getById(id);
    }

    // POST / PUT / DELETE sadece ADMIN (SecurityConfig)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EquipmentResponse create(@Valid @RequestBody EquipmentRequest request,
                                    @AuthenticationPrincipal UserPrincipal principal) {
        return equipmentService.create(request, principal.id());
    }

    @PutMapping("/{id}")
    public EquipmentResponse update(@PathVariable Long id,
                                    @Valid @RequestBody EquipmentRequest request,
                                    @AuthenticationPrincipal UserPrincipal principal) {
        return equipmentService.update(id, request, principal.id());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        equipmentService.delete(id, principal.id());
    }
}
