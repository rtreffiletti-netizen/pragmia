package io.pragmia.minos.api;

import io.pragmia.api.audit.AuditEventType;
import io.pragmia.api.policy.PolicyDecision;
import io.pragmia.api.policy.PolicyRequest;
import io.pragmia.kernel.audit.AuditEventPublisher;
import io.pragmia.minos.engine.SpelPolicyEvaluator;
import io.pragmia.minos.model.Policy;
import io.pragmia.minos.repository.PolicyRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/policies")
@Tag(name = "MINOS — ABAC Policies")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('SCOPE_admin')")
public class PolicyController {

    private final PolicyRepository repo;
    private final SpelPolicyEvaluator evaluator;
    private final AuditEventPublisher audit;

    @GetMapping
    @Operation(summary = "List all policies")
    public List<Policy> list() { return repo.findAll(); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new ABAC policy")
    public Policy create(@Valid @RequestBody Policy policy, @AuthenticationPrincipal Jwt jwt) {
        policy.setCreatedBy(jwt.getSubject());
        var saved = repo.save(policy);
        audit.publish(AuditEventType.POLICY_CREATED, jwt.getSubject(), null, null, null,
            "policy", "CREATE", "OK", null, Map.of("policyId", saved.getId()));
        return saved;
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a policy")
    public Policy update(@PathVariable String id, @Valid @RequestBody Policy patch,
                         @AuthenticationPrincipal Jwt jwt) {
        var existing = repo.findById(id).orElseThrow();
        existing.setName(patch.getName());
        existing.setDescription(patch.getDescription());
        existing.setCondition(patch.getCondition());
        existing.setEffect(patch.getEffect());
        existing.setPriority(patch.getPriority());
        existing.setActive(patch.isActive());
        var saved = repo.save(existing);
        audit.publish(AuditEventType.POLICY_UPDATED, jwt.getSubject(), null, null, null,
            "policy", "UPDATE", "OK", null, Map.of("policyId", id));
        return saved;
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a policy")
    public void delete(@PathVariable String id, @AuthenticationPrincipal Jwt jwt) {
        repo.deleteById(id);
        audit.publish(AuditEventType.POLICY_DELETED, jwt.getSubject(), null, null, null,
            "policy", "DELETE", "OK", null, Map.of("policyId", id));
    }

    @PostMapping("/evaluate")
    @Operation(summary = "Test-evaluate a policy request")
    public Map<String, Object> evaluate(@RequestBody PolicyRequest request) {
        PolicyDecision decision = evaluator.evaluate(request);
        return Map.of("decision", decision.name());
    }
}
