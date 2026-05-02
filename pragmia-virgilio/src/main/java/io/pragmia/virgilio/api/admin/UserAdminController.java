package io.pragmia.virgilio.api.admin;

import io.pragmia.virgilio.api.admin.dto.*;
import io.pragmia.virgilio.user.UserService;
import io.pragmia.virgilio.user.model.VirgilioUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/v1/users")
@RequiredArgsConstructor
@Tag(name = "Admin - Users", description = "Gestione utenti PRAGMIA")
public class UserAdminController {

    private final UserService userService;

    @GetMapping @Operation(summary = "Lista utenti (paginata)")
    public ResponseEntity<PagedResponse<UserDto>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search) {
        Page<VirgilioUser> p = userService.findAll(page, size, search);
        return ResponseEntity.ok(new PagedResponse<>(
            p.getContent().stream().map(this::toDto).toList(),
            p.getNumber(), p.getSize(), p.getTotalElements(), p.getTotalPages()));
    }

    @GetMapping("/{id}") @Operation(summary = "Dettaglio utente")
    public ResponseEntity<UserDto> get(@PathVariable UUID id) {
        return userService.findById(id).map(u -> ResponseEntity.ok(toDto(u)))
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping @Operation(summary = "Crea utente")
    public ResponseEntity<UserDto> create(@Valid @RequestBody CreateUserRequest req,
                                           @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(toDto(userService.createUser(req.username(), req.email(),
                req.password(), req.fullName(), UUID.fromString(jwt.getSubject()))));
    }

    @DeleteMapping("/{id}/disable") @Operation(summary = "Disabilita utente")
    public ResponseEntity<Void> disable(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        userService.disableUser(id, UUID.fromString(jwt.getSubject()));
        return ResponseEntity.noContent().build();
    }

    private UserDto toDto(VirgilioUser u) {
        return new UserDto(u.getId(), u.getUsername(), u.getEmail(), u.getFullName(),
            u.isEnabled(), u.isLocked(), u.isTotpEnabled(),
            u.getRoles().stream().map(r -> r.getName()).collect(Collectors.toSet()),
            u.getCreatedAt(), u.getLastLoginAt(), u.getLoginAttempts());
    }
}
