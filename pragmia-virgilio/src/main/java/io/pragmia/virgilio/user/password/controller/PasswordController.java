
package io.pragmia.virgilio.user.password.controller;

import io.pragmia.virgilio.user.password.service.PasswordChangeService;
import io.pragmia.virgilio.user.password.service.PasswordResetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

/**
 * Self-service password endpoints.
 * /api/v1/password/** — accessibili all'utente autenticato o con token reset
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/password")
@RequiredArgsConstructor
@Tag(name = "Password", description = "Self-service password management")
public class PasswordController {

    private final PasswordResetService resetService;
    private final PasswordChangeService changeService;

    // ── Reset flow ───────────────────────────────────────────────────────────

    @Operation(summary = "Richiedi reset password (invia email con token)")
    @PostMapping("/reset/request")
    public ResponseEntity<Map<String, String>> requestReset(
            @Valid @RequestBody ResetRequestBody body,
            HttpServletRequest request) {

        // Risposta identica indipendentemente dall'esistenza dell'email (anti-enumeration)
        resetService.initiateReset(body.getEmail(), request.getRemoteAddr());
        return ResponseEntity.ok(Map.of(
            "message", "Se l'indirizzo email è registrato riceverai le istruzioni per il reset."
        ));
    }

    @Operation(summary = "Verifica validità token di reset")
    @GetMapping("/reset/validate")
    public ResponseEntity<Map<String, Boolean>> validateToken(@RequestParam String token) {
        boolean valid = resetService.validateToken(token);
        return ResponseEntity.ok(Map.of("valid", valid));
    }

    @Operation(summary = "Conferma reset password con nuovo valore")
    @PostMapping("/reset/confirm")
    public ResponseEntity<Map<String, String>> confirmReset(
            @Valid @RequestBody ResetConfirmBody body,
            HttpServletRequest request) {
        try {
            resetService.confirmReset(body.getToken(), body.getNewPassword(), request.getRemoteAddr());
            return ResponseEntity.ok(Map.of("message", "Password aggiornata con successo."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ── Change flow (utente autenticato) ─────────────────────────────────────

    @Operation(summary = "Cambio password utente autenticato")
    @PostMapping("/change")
    public ResponseEntity<Map<String, String>> changePassword(
            @Valid @RequestBody ChangePasswordBody body,
            Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body(Map.of("error", "Non autenticato"));
        }
        try {
            UUID userId = UUID.fromString(authentication.getName());
            changeService.changePassword(userId, body.getCurrentPassword(), body.getNewPassword());
            return ResponseEntity.ok(Map.of("message", "Password cambiata con successo."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ── Request/Response bodies ──────────────────────────────────────────────

    @Data
    public static class ResetRequestBody {
        @NotBlank @Email
        private String email;
    }

    @Data
    public static class ResetConfirmBody {
        @NotBlank private String token;
        @NotBlank private String newPassword;
    }

    @Data
    public static class ChangePasswordBody {
        @NotBlank private String currentPassword;
        @NotBlank private String newPassword;
    }
}
