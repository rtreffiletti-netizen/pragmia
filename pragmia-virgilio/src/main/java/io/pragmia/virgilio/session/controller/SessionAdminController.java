package io.pragmia.virgilio.session.controller;

import io.pragmia.virgilio.session.model.UserSession;
import io.pragmia.virgilio.session.service.SessionManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/sessions")
@RequiredArgsConstructor
@Tag(name = "Session Management", description = "Session monitoring and administration with hijacking protection")
@PreAuthorize("hasRole('ADMIN')")
public class SessionAdminController {

    private final SessionManagementService sessionManagementService;

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get all active sessions for a user")
    public ResponseEntity<List<UserSession>> getUserSessions(@PathVariable UUID userId) {
        List<UserSession> sessions = sessionManagementService.getActiveSessions(userId);
        return ResponseEntity.ok(sessions);
    }

    @GetMapping("/user/{userId}/count")
    @Operation(summary = "Get active session count for a user")
    public ResponseEntity<Long> getSessionCount(@PathVariable UUID userId) {
        long count = sessionManagementService.getActiveSessionCount(userId);
        return ResponseEntity.ok(count);
    }

    @DeleteMapping("/token/{sessionToken}")
    @Operation(summary = "Revoke a specific session by token")
    public ResponseEntity<Void> revokeSession(@PathVariable String sessionToken) {
        sessionManagementService.revokeSession(sessionToken);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/user/{userId}/others/{currentSessionId}")
    @Operation(summary = "Revoke all other sessions for a user except the current one")
    public ResponseEntity<Integer> revokeOtherSessions(
            @PathVariable UUID userId,
            @PathVariable UUID currentSessionId) {
        int revokedCount = sessionManagementService.revokeOtherSessions(userId, currentSessionId);
        return ResponseEntity.ok(revokedCount);
    }

    @PostMapping("/cleanup")
    @Operation(summary = "Manually trigger expired session cleanup")
    public ResponseEntity<Void> triggerCleanup() {
        sessionManagementService.cleanupExpiredSessions();
        return ResponseEntity.ok().build();
    }
}
