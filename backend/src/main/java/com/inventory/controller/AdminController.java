package com.inventory.controller;

import com.inventory.dto.AuditLogResponse;
import com.inventory.dto.AuthDtos.RoleUpdateRequest;
import com.inventory.dto.AuthDtos.UserResponse;
import com.inventory.model.AuditAction;
import com.inventory.security.UserPrincipal;
import com.inventory.service.AuditLogService;
import com.inventory.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Sadece yöneticilerin erişebildiği işlemler (yetki kontrolü SecurityConfig'te).
 */
@RestController
@RequestMapping("/api")
public class AdminController {

    private final UserService userService;
    private final AuditLogService auditLogService;

    public AdminController(UserService userService, AuditLogService auditLogService) {
        this.userService = userService;
        this.auditLogService = auditLogService;
    }

    @GetMapping("/users")
    public List<UserResponse> getUsers() {
        return userService.getAll();
    }

    @PatchMapping("/users/{id}/role")
    public UserResponse changeRole(@PathVariable Long id,
                                   @Valid @RequestBody RoleUpdateRequest request,
                                   @AuthenticationPrincipal UserPrincipal principal) {
        return userService.changeRole(id, request.role(), principal.id());
    }

    @GetMapping("/audit-logs")
    public List<AuditLogResponse> getAuditLogs(@RequestParam(required = false) AuditAction action) {
        return auditLogService.getLogs(action);
    }
}
