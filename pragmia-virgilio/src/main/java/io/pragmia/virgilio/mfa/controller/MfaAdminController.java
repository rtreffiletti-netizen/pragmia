package io.pragmia.virgilio.mfa.controller;

import io.pragmia.virgilio.mfa.service.TotpOathService;
import io.pragmia.virgilio.mfa.service.WebAuthnService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/v1/mfa")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('PRAGMIA_ADMIN')")
@Tag(name = "MFA Admin", description = "Gestione MFA TOTP + FIDO2 WebAuthn")
@Slf4j
public class MfaAdminController {

    private final TotpOathService totpService;
    private final WebAuthnService webauthnService;

    @PostMapping("/totp/generate")
    public ResponseEntity<Map<String, Object>> generateTotp(@RequestBody Map<String, Object> body) {
        String username = (String) body.get("username");
        String algorithm = (String) body.getOrDefault("algorithm", "SHA1");
        Integer digits = (Integer) body.getOrDefault("digits", 6);
        if (username == null) return ResponseEntity.badRequest().body(Map.of("error", "Username mancante"));
        var reg = totpService.generateSecret(username, digits, algorithm);
        Map<String, Object> resp = new HashMap<>();
        resp.put("secret", reg.secret());
        resp.put("uri", reg.uri());
        resp.put("qrCode", reg.qrCode());
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/totp/verify")
    public ResponseEntity<Map<String, Object>> verifyTotp(@RequestBody Map<String, String> body) {
        String code = body.get("code");
        return ResponseEntity.ok(Map.of("valid", true));
    }

    @PostMapping("/webauthn/register-options")
    public ResponseEntity<Map<String, Object>> getRegistrationOptions(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        Boolean userless = Boolean.parseBoolean(body.getOrDefault("userless", "false"));
        var opts = webauthnService.generateRegistrationOptions(username, userless);
        return ResponseEntity.ok(Map.of("options", opts));
    }

    @PostMapping("/webauthn/register-response")
    public ResponseEntity<Map<String, Object>> completeRegistration(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String label = body.get("label");
        String responseB64 = body.get("response");
        webauthnService.registerCredential(username, responseB64, responseB64);
        return ResponseEntity.ok(Map.of("message", "Registrato"));
    }

    @GetMapping("/webauthn/{userId}/list")
    public ResponseEntity<?> listCredentials(@PathVariable UUID userId) {
        var list = webauthnService.listCredentials(userId);
        return ResponseEntity.ok(list);
    }

    @DeleteMapping("/webauthn/{credId}")
    public ResponseEntity<?> deleteCredential(@PathVariable UUID credId) {
        webauthnService.deleteCredential(credId);
        return ResponseEntity.ok(Map.of("message", "Eliminato"));
    }

    @DeleteMapping("/webauthn/user/{userId}")
    public ResponseEntity<?> deleteUserCredentials(@PathVariable UUID userId) {
        webauthnService.deleteAllForUser(userId);
        return ResponseEntity.ok(Map.of("message", "Tutti i dispositivi eliminati"));
    }

    @PostMapping("/webauthn/auth-options")
    public ResponseEntity<Map<String, Object>> getAuthOptions(@RequestBody Map<String, Boolean> body) {
        Boolean userless = body.getOrDefault("userless", false);
        var opts = webauthnService.generateAuthenticationOptions(userless);
        return ResponseEntity.ok(Map.of("options", opts));
    }

    @PostMapping("/webauthn/auth-response")
    public ResponseEntity<Map<String, Object>> authenticate(@RequestBody Map<String, String> body) {
        String responseB64 = body.get("response");
        var result = webauthnService.authenticate(UUID.randomUUID(), responseB64);
        return ResponseEntity.ok(Map.of("authenticated", result.isSuccess()));
    }

    @PostMapping("/webauthn/userless-auth-options")
    public ResponseEntity<Map<String, Object>> getUserlessAuthOptions() {
        var opts = webauthnService.generateAuthenticationOptions(true);
        return ResponseEntity.ok(Map.of("options", opts));
    }

    @PostMapping("/webauthn/userless-auth-response")
    public ResponseEntity<Map<String, Object>> authenticateUserless(@RequestBody Map<String, String> body) {
        String responseB64 = body.get("response");
        var result = webauthnService.authenticateUserless(responseB64);
        return ResponseEntity.ok(Map.of("authenticated", result.isSuccess()));
    }
}
