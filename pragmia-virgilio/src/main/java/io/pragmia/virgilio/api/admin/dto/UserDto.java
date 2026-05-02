package io.pragmia.virgilio.api.admin.dto;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record UserDto(UUID id, String username, String email, String fullName,
                       boolean enabled, boolean locked, boolean totpEnabled,
                       Set<String> roles, Instant createdAt, Instant lastLoginAt, int loginAttempts) {}
