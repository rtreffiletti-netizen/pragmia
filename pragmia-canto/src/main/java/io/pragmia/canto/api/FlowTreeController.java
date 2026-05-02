package io.pragmia.canto.api;

import io.pragmia.canto.model.FlowTree;
import io.pragmia.canto.service.FlowTreeService;
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

@RestController
@RequestMapping("/api/admin/flows")
@Tag(name = "CANTO — Flow Trees")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('SCOPE_admin')")
public class FlowTreeController {

    private final FlowTreeService service;

    @GetMapping
    @Operation(summary = "List all flow trees")
    public List<FlowTree> list() { return service.findAll(); }

    @GetMapping("/{id}")
    @Operation(summary = "Get flow tree by ID")
    public FlowTree get(@PathVariable String id) { return service.findById(id); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new flow tree")
    public FlowTree create(@Valid @RequestBody FlowTree tree, @AuthenticationPrincipal Jwt jwt) {
        return service.create(tree, jwt.getSubject());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a flow tree")
    public FlowTree update(@PathVariable String id, @Valid @RequestBody FlowTree tree,
                           @AuthenticationPrincipal Jwt jwt) {
        return service.update(id, tree, jwt.getSubject());
    }

    @PostMapping("/{id}/activate")
    @Operation(summary = "Activate a flow tree (deactivates all others)")
    public void activate(@PathVariable String id, @AuthenticationPrincipal Jwt jwt) {
        service.activate(id, jwt.getSubject());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a flow tree")
    public void delete(@PathVariable String id, @AuthenticationPrincipal Jwt jwt) {
        service.delete(id, jwt.getSubject());
    }
}
