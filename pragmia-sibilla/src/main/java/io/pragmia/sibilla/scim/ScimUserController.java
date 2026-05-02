package io.pragmia.sibilla.scim;

import io.pragmia.api.audit.AuditEventType;
import io.pragmia.kernel.audit.AuditEventPublisher;
import io.pragmia.sibilla.scim.model.ScimListResponse;
import io.pragmia.sibilla.scim.model.ScimUser;
import io.pragmia.sibilla.service.ProvisioningService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/scim/v2/Users",
    produces = "application/scim+json")
@Tag(name = "SIBILLA — SCIM 2.0")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('SCOPE_scim')")
public class ScimUserController {

    private final ProvisioningService provisioningService;
    private final AuditEventPublisher audit;

    @GetMapping
    @Operation(summary = "SCIM: List Users")
    public ScimListResponse<ScimUser> list(
        @RequestParam(defaultValue = "1")   int startIndex,
        @RequestParam(defaultValue = "100") int count,
        @RequestParam(required = false)     String filter) {

        List<ScimUser> users = provisioningService.listUsers(filter, startIndex - 1, count);
        return ScimListResponse.<ScimUser>builder()
            .totalResults(users.size())
            .startIndex(startIndex)
            .itemsPerPage(count)
            .resources(users)
            .build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "SCIM: Get User by ID")
    public ScimUser get(@PathVariable String id) {
        return provisioningService.getUser(id);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "SCIM: Create User (provision)")
    public ScimUser create(@RequestBody ScimUser user, @AuthenticationPrincipal Jwt jwt) {
        var created = provisioningService.createUser(user);
        audit.publish(AuditEventType.ADMIN_USER_CREATED, jwt.getSubject(), null, null, null,
            "scim_user", "CREATE", "OK", null,
            Map.of("scimUserId", created.getId(), "userName", created.getUserName()));
        return created;
    }

    @PutMapping("/{id}")
    @Operation(summary = "SCIM: Replace User")
    public ScimUser replace(@PathVariable String id, @RequestBody ScimUser user,
                            @AuthenticationPrincipal Jwt jwt) {
        user.setId(id);
        var updated = provisioningService.updateUser(user);
        audit.publish(AuditEventType.ADMIN_USER_UPDATED, jwt.getSubject(), null, null, null,
            "scim_user", "REPLACE", "OK", null, Map.of("scimUserId", id));
        return updated;
    }

    @PatchMapping("/{id}")
    @Operation(summary = "SCIM: Patch User (activate/deactivate)")
    public ScimUser patch(@PathVariable String id, @RequestBody Map<String, Object> patch,
                          @AuthenticationPrincipal Jwt jwt) {
        var updated = provisioningService.patchUser(id, patch);
        audit.publish(AuditEventType.ADMIN_USER_UPDATED, jwt.getSubject(), null, null, null,
            "scim_user", "PATCH", "OK", null, Map.of("scimUserId", id));
        return updated;
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "SCIM: Delete User (deprovision)")
    public void delete(@PathVariable String id, @AuthenticationPrincipal Jwt jwt) {
        provisioningService.deleteUser(id);
        audit.publish(AuditEventType.ADMIN_USER_DISABLED, jwt.getSubject(), null, null, null,
            "scim_user", "DELETE", "OK", null, Map.of("scimUserId", id));
    }
}
