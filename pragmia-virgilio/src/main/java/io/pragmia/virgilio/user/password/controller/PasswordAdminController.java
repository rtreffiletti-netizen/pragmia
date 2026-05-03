
package io.pragmia.virgilio.user.password.controller;

import io.pragmia.virgilio.user.password.model.PasswordPolicy;
import io.pragmia.virgilio.user.password.repository.PasswordPolicyRepository;
import io.pragmia.virgilio.user.password.service.PasswordChangeService;
import io.pragmia.virgilio.user.password.service.PasswordPolicyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

/**
 * Admin API per gestione password policy e reset forzato.
 * Richiede scope pragmia:admin.
 */
@RestController
@RequestMapping("/api/admin/v1/password")
@RequiredArgsConstructor
@Tag(name = "Password Admin", description = "Admin password management — PRAGMIA")
public class PasswordAdminController {

    private final PasswordPolicyService policyService;
    private final PasswordPolicyRepository policyRepository;
    private final PasswordChangeService changeService;

    @Operation(summary = "Leggi policy password attiva")
    @GetMapping("/policy")
    @PreAuthorize("hasAuthority('SCOPE_pragmia:admin')")
    public ResponseEntity<PasswordPolicy> getPolicy() {
        return ResponseEntity.ok(policyService.getActivePolicy());
    }

    @Operation(summary = "Aggiorna policy password")
    @PutMapping("/policy")
    @PreAuthorize("hasAuthority('SCOPE_pragmia:admin')")
    public ResponseEntity<PasswordPolicy> updatePolicy(@Valid @RequestBody PasswordPolicy policy) {
        policy.setId("default");
        return ResponseEntity.ok(policyRepository.save(policy));
    }

    @Operation(summary = "Forza reset password utente (admin)")
    @PostMapping("/users/{userId}/force-reset")
    @PreAuthorize("hasAuthority('SCOPE_pragmia:admin')")
    public ResponseEntity<Map<String, String>> forceReset(
            @PathVariable UUID userId,
            @Valid @RequestBody ForceResetBody body,
            Authentication adminAuth) {
        try {
            UUID adminId = adminAuth != null ? UUID.fromString(adminAuth.getName()) : null;
            changeService.adminForceReset(userId, body.getNewPassword(), adminId);
            return ResponseEntity.ok(Map.of("message", "Password aggiornata dall'amministratore."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(summary = "Valida password rispetto alla policy attiva")
    @PostMapping("/policy/validate")
    @PreAuthorize("hasAuthority('SCOPE_pragmia:admin')")
    public ResponseEntity<Map<String, Object>> validatePassword(@RequestBody Map<String, String> body) {
        String pwd = body.get("password");
        var violations = policyService.validate(pwd, policyService.getActivePolicy());
        return ResponseEntity.ok(Map.of(
            "valid", violations.isEmpty(),
            "violations", violations
        ));
    }

    @Data
    public static class ForceResetBody {
        @NotBlank private String newPassword;
    }
}
