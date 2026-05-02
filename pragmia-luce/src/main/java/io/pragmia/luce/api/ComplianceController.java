package io.pragmia.luce.api;

import io.pragmia.luce.model.ComplianceFramework;
import io.pragmia.luce.model.ComplianceReport;
import io.pragmia.luce.repository.ComplianceReportRepository;
import io.pragmia.luce.service.ComplianceReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/compliance")
@Tag(name = "LUCE — Compliance")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('SCOPE_admin')")
public class ComplianceController {

    private final ComplianceReportService service;
    private final ComplianceReportRepository repo;

    @PostMapping("/{framework}/generate")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Generate a compliance report for a given framework")
    public ComplianceReport generate(@PathVariable ComplianceFramework framework,
                                     @AuthenticationPrincipal Jwt jwt) {
        return service.generate(framework, jwt.getSubject());
    }

    @GetMapping("/{framework}/reports")
    @Operation(summary = "List compliance reports for a framework")
    public Page<ComplianceReport> reports(@PathVariable ComplianceFramework framework,
                                          Pageable pageable) {
        return repo.findByFrameworkOrderByGeneratedAtDesc(framework, pageable);
    }

    @GetMapping("/reports/{id}")
    @Operation(summary = "Get a specific compliance report")
    public ComplianceReport get(@PathVariable String id) {
        return repo.findById(id).orElseThrow();
    }
}
