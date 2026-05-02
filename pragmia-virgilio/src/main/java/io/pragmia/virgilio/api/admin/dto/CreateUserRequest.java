package io.pragmia.virgilio.api.admin.dto;

import jakarta.validation.constraints.*;

public record CreateUserRequest(
    @NotBlank @Size(min = 3, max = 64) String username,
    @Email @NotBlank                   String email,
    @NotBlank @Size(min = 8)           String password,
                                       String fullName
) {}
