package io.pragmia.beatrice.api;

import io.pragmia.beatrice.model.NlpCommand;
import io.pragmia.beatrice.service.BeatriceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/beatrice")
@Tag(name = "BEATRICE — NLP Admin")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('SCOPE_admin')")
public class BeatriceController {

    private final BeatriceService service;

    @PostMapping("/ask")
    @Operation(summary = "Submit a natural language admin command")
    public NlpCommand ask(@RequestBody String prompt, @AuthenticationPrincipal Jwt jwt) {
        return service.submit(prompt, jwt.getSubject());
    }

    @GetMapping("/pending")
    @Operation(summary = "List commands pending dual-approval")
    public List<NlpCommand> pending() {
        return service.getPending();
    }

    @PostMapping("/{id}/approve")
    @Operation(summary = "Approve a pending command (dual-approval)")
    public NlpCommand approve(@PathVariable String id, @AuthenticationPrincipal Jwt jwt) {
        return service.approve(id, jwt.getSubject());
    }

    @PostMapping("/{id}/reject")
    @Operation(summary = "Reject a pending command")
    public NlpCommand reject(@PathVariable String id,
                             @RequestBody(required = false) String notes,
                             @AuthenticationPrincipal Jwt jwt) {
        return service.reject(id, jwt.getSubject(), notes);
    }
}
