package io.pragmia.saml.controller;

import io.pragmia.saml.dto.SamlSpRegistrationRequest;
import io.pragmia.saml.dto.SamlSpRegistrationResponse;
import io.pragmia.saml.dto.SamlSessionDto;
import io.pragmia.saml.model.SamlServiceProvider;
import io.pragmia.saml.model.SamlSession;
import io.pragmia.saml.repository.SamlAuditEventRepository;
import io.pragmia.saml.repository.SamlSessionRepository;
import io.pragmia.saml.service.SamlSpManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Admin API per gestione SAML: SP registration, session management, audit.
 * Richiede scope pragmia:admin (token JWT da VIRGILIO).
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/v1/saml")
@Tag(name = "SAML Admin", description = "Gestione SAML IdP/SP — modulo PRAGMIA-SAML")
@ConditionalOnProperty(name = "pragmia.modules.saml.enabled", havingValue = "true", matchIfMissing = true)
public class SamlAdminController {

    private final SamlSpManagementService spManagementService;
    private final SamlSessionRepository sessionRepository;
    private final SamlAuditEventRepository auditRepository;

    // ── Service Providers ───────────────────────────────────────────────────

    @Operation(summary = "Registra un nuovo Service Provider")
    @PostMapping("/service-providers")
    @PreAuthorize("hasAuthority('SCOPE_pragmia:admin')")
    public ResponseEntity<SamlSpRegistrationResponse> registerSp(
            @Valid @RequestBody SamlSpRegistrationRequest request) {
        return ResponseEntity.ok(spManagementService.registerSp(request));
    }

    @Operation(summary = "Lista SP registrati e attivi")
    @GetMapping("/service-providers")
    @PreAuthorize("hasAuthority('SCOPE_pragmia:admin')")
    public ResponseEntity<List<SamlServiceProvider>> listSps() {
        return ResponseEntity.ok(spManagementService.listEnabledSps());
    }

    @Operation(summary = "Rimuovi un SP")
    @DeleteMapping("/service-providers/{id}")
    @PreAuthorize("hasAuthority('SCOPE_pragmia:admin')")
    public ResponseEntity<Void> deleteSp(@PathVariable String id) {
        spManagementService.deleteSp(id);
        return ResponseEntity.noContent().build();
    }

    // ── Sessions ─────────────────────────────────────────────────────────────

    @Operation(summary = "Lista sessioni SAML attive di un utente")
    @GetMapping("/sessions/user/{userId}")
    @PreAuthorize("hasAuthority('SCOPE_pragmia:admin')")
    public ResponseEntity<List<SamlSessionDto>> getUserSessions(@PathVariable String userId) {
        List<SamlSessionDto> sessions = sessionRepository.findByUserIdAndActiveTrue(userId)
            .stream().map(this::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(sessions);
    }

    @Operation(summary = "Termina tutte le sessioni SAML di un utente (SLO forzato)")
    @DeleteMapping("/sessions/user/{userId}")
    @PreAuthorize("hasAuthority('SCOPE_pragmia:admin')")
    public ResponseEntity<Void> killUserSamlSessions(@PathVariable String userId) {
        sessionRepository.deactivateAllByUserId(userId);
        log.warn("[PRAGMIA-SAML] Sessioni SAML terminate per utente: {}", userId);
        return ResponseEntity.noContent().build();
    }

    // ── Audit ───────────────────────────────────────────────────────────────

    @Operation(summary = "Audit log SAML (paginato)")
    @GetMapping("/audit")
    @PreAuthorize("hasAuthority('SCOPE_pragmia:admin')")
    public ResponseEntity<?> getAuditLog(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(auditRepository.findAll(
            PageRequest.of(page, size, Sort.by("timestamp").descending())));
    }

    // ── Health ───────────────────────────────────────────────────────────────

    @Operation(summary = "Stato modulo SAML")
    @GetMapping("/status")
    public ResponseEntity<?> status() {
        long activeSessions = sessionRepository.count();
        return ResponseEntity.ok(java.util.Map.of(
            "module", "pragmia-saml",
            "status", "UP",
            "activeSamlSessions", activeSessions
        ));
    }

    private SamlSessionDto toDto(SamlSession s) {
        return SamlSessionDto.builder()
            .id(s.getId()).userId(s.getUserId()).spEntityId(s.getSpEntityId())
            .nameId(s.getNameId()).sessionIndex(s.getSessionIndex())
            .issuedAt(s.getIssuedAt()).expiresAt(s.getExpiresAt())
            .active(s.isActive()).authType(s.getAuthType()).clientIp(s.getClientIp())
            .build();
    }
}
