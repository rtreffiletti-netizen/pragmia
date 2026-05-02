package io.pragmia.clio.api;

import io.pragmia.api.audit.AuditEventType;
import io.pragmia.clio.model.AuditRecord;
import io.pragmia.clio.repository.AuditRecordRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/admin/audit")
@Tag(name = "CLIO — Audit Log")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('SCOPE_admin')")
public class AuditLogController {

    private final AuditRecordRepository repo;

    @GetMapping
    @Operation(summary = "List audit events (paginated)")
    public Page<AuditRecord> list(Pageable pageable) {
        return repo.findAll(pageable);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Audit events by user")
    public Page<AuditRecord> byUser(@PathVariable String userId, Pageable pageable) {
        return repo.findByUserId(userId, pageable);
    }

    @GetMapping("/type/{type}")
    @Operation(summary = "Audit events by type")
    public Page<AuditRecord> byType(@PathVariable AuditEventType type, Pageable pageable) {
        return repo.findByEventType(type, pageable);
    }

    @GetMapping("/session/{sessionId}")
    @Operation(summary = "Audit events by session")
    public Page<AuditRecord> bySession(@PathVariable String sessionId, Pageable pageable) {
        return repo.findBySessionId(sessionId, pageable);
    }

    @GetMapping("/range")
    @Operation(summary = "Audit events in time range")
    public Page<AuditRecord> byRange(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
        Pageable pageable) {
        return repo.findByTsBetween(from, to, pageable);
    }
}
