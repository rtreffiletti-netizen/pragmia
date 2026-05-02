package io.pragmia.virgilio.access.controller;

import io.pragmia.virgilio.access.model.RiskFactor;
import io.pragmia.virgilio.access.model.RiskProfile;
import io.pragmia.virgilio.access.service.RiskEngine;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/conditional-access")
@RequiredArgsConstructor
@Tag(name = "Conditional Access Administration", description = "Risk-based authentication and conditional access management")
@PreAuthorize("hasRole('ADMIN')")
public class ConditionalAccessAdminController {

    private final RiskEngine riskEngine;

    @GetMapping("/risk-factors")
    @Operation(summary = "List all risk factors")
    public ResponseEntity<List<RiskFactor>> listRiskFactors() {
        return ResponseEntity.ok(riskEngine.getAllRiskFactors());
    }

    @GetMapping("/risk-factors/{id}")
    @Operation(summary = "Get risk factor by ID")
    public ResponseEntity<RiskFactor> getRiskFactor(@PathVariable UUID id) {
        return riskEngine.getRiskFactorById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/risk-factors")
    @Operation(summary = "Create new risk factor")
    public ResponseEntity<RiskFactor> createRiskFactor(@RequestBody RiskFactor riskFactor) {
        RiskFactor created = riskEngine.createRiskFactor(riskFactor);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/risk-factors/{id}")
    @Operation(summary = "Update risk factor")
    public ResponseEntity<RiskFactor> updateRiskFactor(
            @PathVariable UUID id,
            @RequestBody RiskFactor riskFactor) {
        return riskEngine.updateRiskFactor(id, riskFactor)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/risk-factors/{id}")
    @Operation(summary = "Delete risk factor")
    public ResponseEntity<Void> deleteRiskFactor(@PathVariable UUID id) {
        riskEngine.deleteRiskFactor(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/risk-profiles")
    @Operation(summary = "List all risk profiles")
    public ResponseEntity<List<RiskProfile>> listRiskProfiles() {
        return ResponseEntity.ok(riskEngine.getAllRiskProfiles());
    }

    @GetMapping("/risk-profiles/{id}")
    @Operation(summary = "Get risk profile by ID")
    public ResponseEntity<RiskProfile> getRiskProfile(@PathVariable UUID id) {
        return riskEngine.getRiskProfileById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/risk-profiles")
    @Operation(summary = "Create new risk profile")
    public ResponseEntity<RiskProfile> createRiskProfile(@RequestBody RiskProfile riskProfile) {
        RiskProfile created = riskEngine.createRiskProfile(riskProfile);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/risk-profiles/{id}")
    @Operation(summary = "Update risk profile")
    public ResponseEntity<RiskProfile> updateRiskProfile(
            @PathVariable UUID id,
            @RequestBody RiskProfile riskProfile) {
        return riskEngine.updateRiskProfile(id, riskProfile)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/risk-profiles/{id}")
    @Operation(summary = "Delete risk profile")
    public ResponseEntity<Void> deleteRiskProfile(@PathVariable UUID id) {
        riskEngine.deleteRiskProfile(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/evaluate")
    @Operation(summary = "Evaluate risk for a login context")
    public ResponseEntity<Integer> evaluateRisk(@RequestBody io.pragmia.virgilio.access.model.LoginContext loginContext) {
        int riskScore = riskEngine.evaluateRisk(loginContext);
        return ResponseEntity.ok(riskScore);
    }
}
