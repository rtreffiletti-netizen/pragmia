package io.pragmia.virgilio.oauth.controller;

import io.pragmia.virgilio.oauth.model.OAuthAccessToken;
import io.pragmia.virgilio.oauth.repository.OAuthAccessTokenRepository;
import io.pragmia.virgilio.user.model.User;
import io.pragmia.virgilio.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * OIDC UserInfo Endpoint
 * OpenID Connect Core 1.0 - Section 5.3
 * Returns claims about authenticated user
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class UserInfoEndpoint {

    private final OAuthAccessTokenRepository accessTokenRepository;
    private final UserService userService;

    /**
     * UserInfo Endpoint
     * GET /oauth2/userinfo
     * POST /oauth2/userinfo
     * 
     * Requires Bearer token in Authorization header
     * Returns user claims based on granted scopes
     */
    @GetMapping(value = "/oauth2/userinfo", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getUserInfoGet(@RequestHeader("Authorization") String authHeader) {
        return getUserInfo(authHeader);
    }

    @PostMapping(value = "/oauth2/userinfo", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getUserInfoPost(@RequestHeader("Authorization") String authHeader) {
        return getUserInfo(authHeader);
    }

    private ResponseEntity<?> getUserInfo(String authHeader) {
        try {
            // Extract Bearer token
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return errorResponse("invalid_request", "Missing or invalid Authorization header");
            }

            String token = authHeader.substring("Bearer ".length());

            // Validate access token
            Optional<OAuthAccessToken> accessTokenOpt = accessTokenRepository.findByToken(token);
            if (accessTokenOpt.isEmpty()) {
                return errorResponse("invalid_token", "Invalid access token");
            }

            OAuthAccessToken accessToken = accessTokenOpt.get();

            // Check token expiration
            if (accessToken.getExpiresAt().isBefore(Instant.now())) {
                return errorResponse("invalid_token", "Access token expired");
            }

            // Get user information
            String userId = accessToken.getUserId();
            if (userId == null) {
                return errorResponse("invalid_token", "Token not associated with a user");
            }

            Optional<User> userOpt = userService.findById(userId);
            if (userOpt.isEmpty()) {
                return errorResponse("invalid_token", "User not found");
            }

            User user = userOpt.get();
            String scope = accessToken.getScope() != null ? accessToken.getScope() : "";

            // Build claims based on scopes
            Map<String, Object> claims = buildUserClaims(user, scope);

            log.info("UserInfo request successful for user: {}", userId);
            return ResponseEntity.ok(claims);

        } catch (Exception e) {
            log.error("Error processing userinfo request", e);
            return errorResponse("server_error", "Internal server error");
        }
    }

    /**
     * Build user claims based on granted scopes
     */
    private Map<String, Object> buildUserClaims(User user, String scope) {
        Map<String, Object> claims = new HashMap<>();
        
        // Always include sub (subject identifier)
        claims.put("sub", user.getId());

        // Profile scope claims
        if (scope.contains("profile")) {
            addIfNotNull(claims, "name", user.getName());
            addIfNotNull(claims, "given_name", user.getGivenName());
            addIfNotNull(claims, "family_name", user.getFamilyName());
            addIfNotNull(claims, "middle_name", user.getMiddleName());
            addIfNotNull(claims, "nickname", user.getNickname());
            addIfNotNull(claims, "preferred_username", user.getUsername());
            addIfNotNull(claims, "profile", user.getProfile());
            addIfNotNull(claims, "picture", user.getPicture());
            addIfNotNull(claims, "website", user.getWebsite());
            addIfNotNull(claims, "gender", user.getGender());
            addIfNotNull(claims, "birthdate", user.getBirthdate());
            addIfNotNull(claims, "zoneinfo", user.getZoneinfo());
            addIfNotNull(claims, "locale", user.getLocale());
            addIfNotNull(claims, "updated_at", user.getUpdatedAt() != null ? 
                user.getUpdatedAt().getEpochSecond() : null);
        }

        // Email scope claims
        if (scope.contains("email")) {
            addIfNotNull(claims, "email", user.getEmail());
            addIfNotNull(claims, "email_verified", user.getEmailVerified());
        }

        // Phone scope claims
        if (scope.contains("phone")) {
            addIfNotNull(claims, "phone_number", user.getPhoneNumber());
            addIfNotNull(claims, "phone_number_verified", user.getPhoneNumberVerified());
        }

        // Address scope claims
        if (scope.contains("address")) {
            if (user.getAddress() != null) {
                Map<String, String> address = new HashMap<>();
                address.put("formatted", user.getAddress());
                // Could be expanded with: street_address, locality, region, postal_code, country
                claims.put("address", address);
            }
        }

        return claims;
    }

    private void addIfNotNull(Map<String, Object> map, String key, Object value) {
        if (value != null) {
            map.put(key, value);
        }
    }

    private ResponseEntity<?> errorResponse(String error, String description) {
        Map<String, String> errorBody = Map.of(
            "error", error,
            "error_description", description
        );
        
        HttpStatus status = switch (error) {
            case "invalid_token" -> HttpStatus.UNAUTHORIZED;
            case "insufficient_scope" -> HttpStatus.FORBIDDEN;
            default -> HttpStatus.BAD_REQUEST;
        };

        return ResponseEntity.status(status).body(errorBody);
    }
}
