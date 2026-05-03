package io.pragmia.virgilio.oauth.service;

import io.pragmia.virgilio.oauth.model.*;
import io.pragmia.virgilio.oauth.repository.*;
import io.pragmia.virgilio.access.model.User;
import io.pragmia.virgilio.access.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthorizationService {

    private final OAuthAuthorizationCodeRepository authorizationCodeRepository;
    private final OAuthClientRepository clientRepository;
    private final UserRepository userRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    private static final int AUTHORIZATION_CODE_LENGTH = 32;
    private static final int CODE_EXPIRATION_SECONDS = 600; // 10 minutes

    /**
     * Validates authorization request parameters
     */
    public void validateAuthorizationRequest(
            String clientId,
            String redirectUri,
            String responseType,
            String scope) {
        
        if (clientId == null || clientId.isBlank()) {
            throw new IllegalArgumentException("client_id is required");
        }

        if (redirectUri == null || redirectUri.isBlank()) {
            throw new IllegalArgumentException("redirect_uri is required");
        }

        if (responseType == null || !"code".equals(responseType)) {
            throw new IllegalArgumentException("response_type must be 'code'");
        }

        // Validate client exists
        OAuthClient client = clientRepository.findByClientId(clientId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid client_id"));

        // Validate redirect URI
        if (!isValidRedirectUri(client, redirectUri)) {
            throw new IllegalArgumentException("Invalid redirect_uri");
        }

        // Validate scope
        if (scope != null && !isValidScope(client, scope)) {
            throw new IllegalArgumentException("Invalid scope");
        }

        log.info("Authorization request validated for client: {}", clientId);
    }

    /**
     * Generates and stores an authorization code
     */
    @Transactional
    public OAuthAuthorizationCode generateAuthorizationCode(
            String clientId,
            String userId,
            String redirectUri,
            String scope,
            String nonce,
            String codeChallenge,
            String codeChallengeMethod) {

        String code = generateSecureCode();
        Instant expiresAt = Instant.now().plusSeconds(CODE_EXPIRATION_SECONDS);

        OAuthAuthorizationCode authorizationCode = new OAuthAuthorizationCode();
        authorizationCode.setCode(code);
        authorizationCode.setClientId(clientId);
        authorizationCode.setUserId(userId);
        authorizationCode.setRedirectUri(redirectUri);
        authorizationCode.setScope(scope);
        authorizationCode.setNonce(nonce);
        authorizationCode.setCodeChallenge(codeChallenge);
        authorizationCode.setCodeChallengeMethod(codeChallengeMethod);
        authorizationCode.setExpiresAt(expiresAt);
        authorizationCode.setUsed(false);
        authorizationCode.setCreatedAt(Instant.now());

        authorizationCodeRepository.save(authorizationCode);

        log.info("Authorization code generated for client: {} and user: {}", clientId, userId);
        return authorizationCode;
    }

    /**
     * Validates and retrieves an authorization code
     */
    @Transactional
    public OAuthAuthorizationCode validateAndConsumeAuthorizationCode(
            String code,
            String clientId,
            String redirectUri,
            String codeVerifier) {

        OAuthAuthorizationCode authorizationCode = authorizationCodeRepository
                .findByCodeAndClientId(code, clientId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid authorization code"));

        // Check if already used
        if (authorizationCode.isUsed()) {
            log.warn("Authorization code already used: {}", code);
            throw new IllegalArgumentException("Authorization code already used");
        }

        // Check expiration
        if (authorizationCode.getExpiresAt().isBefore(Instant.now())) {
            log.warn("Authorization code expired: {}", code);
            throw new IllegalArgumentException("Authorization code expired");
        }

        // Validate redirect URI
        if (!authorizationCode.getRedirectUri().equals(redirectUri)) {
            log.warn("Redirect URI mismatch for code: {}", code);
            throw new IllegalArgumentException("Invalid redirect_uri");
        }

        // Validate PKCE if present
        if (authorizationCode.getCodeChallenge() != null) {
            validatePKCE(authorizationCode, codeVerifier);
        }

        // Mark as used
        authorizationCode.setUsed(true);
        authorizationCodeRepository.save(authorizationCode);

        log.info("Authorization code consumed for client: {}", clientId);
        return authorizationCode;
    }

    /**
     * Checks if redirect URI is valid for the client
     */
    private boolean isValidRedirectUri(OAuthClient client, String redirectUri) {
        if (client.getRedirectUris() == null || client.getRedirectUris().isEmpty()) {
            return false;
        }
        return client.getRedirectUris().contains(redirectUri);
    }

    /**
     * Checks if requested scope is valid for the client
     */
    private boolean isValidScope(OAuthClient client, String requestedScope) {
        if (requestedScope == null || requestedScope.isBlank()) {
            return true;
        }

        Set<String> requestedScopes = new HashSet<>(Arrays.asList(requestedScope.split(" ")));
        Set<String> allowedScopes = new HashSet<>(client.getAllowedScopes());

        return allowedScopes.containsAll(requestedScopes);
    }

    /**
     * Validates PKCE code challenge
     */
    private void validatePKCE(OAuthAuthorizationCode authorizationCode, String codeVerifier) {
        if (codeVerifier == null || codeVerifier.isBlank()) {
            throw new IllegalArgumentException("code_verifier is required for PKCE");
        }

        String method = authorizationCode.getCodeChallengeMethod();
        String challenge = authorizationCode.getCodeChallenge();

        String computedChallenge;
        if ("plain".equals(method)) {
            computedChallenge = codeVerifier;
        } else if ("S256".equals(method)) {
            computedChallenge = computeS256Challenge(codeVerifier);
        } else {
            throw new IllegalArgumentException("Invalid code_challenge_method");
        }

        if (!computedChallenge.equals(challenge)) {
            log.warn("PKCE validation failed");
            throw new IllegalArgumentException("Invalid code_verifier");
        }
    }

    /**
     * Computes S256 challenge from verifier
     */
    private String computeS256Challenge(String codeVerifier) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(codeVerifier.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Failed to compute S256 challenge", e);
        }
    }

    /**
     * Generates a secure random code
     */
    private String generateSecureCode() {
        byte[] randomBytes = new byte[AUTHORIZATION_CODE_LENGTH];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    /**
     * Gets user consent details
     */
    public Map<String, Object> getConsentDetails(String clientId, String scope, String userId) {
        OAuthClient client = clientRepository.findByClientId(clientId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid client_id"));

        User user = userRepository.findById(Long.parseLong(userId))
                .orElseThrow(() -> new IllegalArgumentException("Invalid user"));

        List<String> requestedScopes = scope != null ? 
                Arrays.asList(scope.split(" ")) : Collections.emptyList();

        Map<String, Object> consentDetails = new HashMap<>();
        consentDetails.put("clientName", client.getClientName());
        consentDetails.put("clientId", clientId);
        consentDetails.put("userName", user.getUsername());
        consentDetails.put("requestedScopes", requestedScopes);
        consentDetails.put("scopeDescriptions", getScopeDescriptions(requestedScopes));

        return consentDetails;
    }

    /**
     * Gets human-readable scope descriptions
     */
    private Map<String, String> getScopeDescriptions(List<String> scopes) {
        Map<String, String> descriptions = new HashMap<>();
        for (String scope : scopes) {
            switch (scope) {
                case "openid":
                    descriptions.put(scope, "Access your identity information");
                    break;
                case "profile":
                    descriptions.put(scope, "Access your profile information");
                    break;
                case "email":
                    descriptions.put(scope, "Access your email address");
                    break;
                case "offline_access":
                    descriptions.put(scope, "Maintain access when you're not present");
                    break;
                default:
                    descriptions.put(scope, "Access " + scope);
            }
        }
        return descriptions;
    }
}
