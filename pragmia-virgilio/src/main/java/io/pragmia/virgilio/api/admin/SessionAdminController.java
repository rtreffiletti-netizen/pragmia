package io.pragmia.virgilio.api.admin;

import io.pragmia.virgilio.session.SessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/v1/sessions")
@RequiredArgsConstructor
@Tag(name = "Admin - Sessions", description = "Gestione sessioni attive")
public class SessionAdminController {

    private final SessionService sessionService;

    @GetMapping("/count") @Operation(summary = "Numero sessioni attive")
    public ResponseEntity<Map<String, Long>> count() {
        return ResponseEntity.ok(Map.of("activeSessions", sessionService.countActiveSessions()));
    }

    @DeleteMapping("/{sessionId}") @Operation(summary = "Termina sessione specifica")
    public ResponseEntity<Map<String, Object>> kill(@PathVariable String sessionId,
                                                     @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(Map.of("sessionId", sessionId,
            "killed", sessionService.killSession(sessionId, UUID.fromString(jwt.getSubject()))));
    }

    @DeleteMapping("/user/{principalName}") @Operation(summary = "Termina tutte le sessioni di un utente")
    public ResponseEntity<Map<String, Object>> killByUser(@PathVariable String principalName,
                                                           @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(Map.of("principalName", principalName,
            "sessionsKilled", sessionService.killSessionsByUser(principalName,
                UUID.fromString(jwt.getSubject()))));
    }

    @DeleteMapping @Operation(summary = "CRITICAL — Kill ALL active sessions")
    public ResponseEntity<Map<String, Object>> killAll(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(Map.of("operation", "KILL_ALL",
            "sessionsKilled", sessionService.killAllSessions(UUID.fromString(jwt.getSubject()))));
    }
}
