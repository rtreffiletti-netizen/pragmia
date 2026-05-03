package io.pragmia.virgilio.oauth.service;

import io.pragmia.virgilio.oauth.model.*;
import io.pragmia.virgilio.oauth.repository.*;
import io.pragmia.virgilio.jwt.service.JwtService;
import io.pragmia.virgilio.user.model.User;
import io.pragmia.virgilio.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.*;

/**
 * Token Service
 * Handles OAuth2 token lifecycle and grant type processing
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TokenService {

    private final OAuthClientRepository clientRepository;
    private final OAuthAuthorizationCodeRepository authCodeRepository;
    private final OAuthAccessTokenRepository accessTokenRepository;
    private final OAuthRefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @Value("${virgilio.oauth.access-token.ttl:3600}")
    private long accessTokenTtl;

    @Value("${virgilio.oauth.refresh-token.ttl:86400}")
    private long refreshTokenTtl;

    @Value("${virgilio.oauth.device-code.ttl:1800}")
    private long deviceCodeTtl;

    @Value("${virgilio.oauth.authorization-code.ttl:600}")
    private long authorizationCodeTtl;

    /**
     * Process token request based on grant type
     */
    @Transactional
    public TokenResponse processTokenRequest(TokenRequest request) {
        String grantType = request.getGrantType();
        
        if (grantType == null) {
            throw new IllegalArgumentException("grant_type is required");
        }

        return switch (grantType) {
            case "authorization_code" -> processAuthorizationCodeGrant(request);
            case "refresh_token" -> processRefreshTokenGrant(request);
            case "client_credentials" -> processClientCredentialsGrant(request);
            case "password" -> processPasswordGrant(request);
            case "urn:ietf:params:oauth:grant-type:device_code" -> processDeviceCodeGrant(request);
            case "urn:ietf:params:oauth:grant-type:token-exchange" -> processTokenExchangeGrant(request);
            default -> throw new IllegalArgumentException("Unsupported grant_type: " + grantType);
        };
    }

    /**
     * Authorization Code Grant
     * RFC 6749 - Section 4.1
     */
    private TokenResponse processAuthorizationCodeGrant(TokenRequest request) {
        if (request.getCode() == null) {
            throw new IllegalArgumentException("code is required");
        }

        OAuthAuthorizationCode authCode = authCodeRepository.findByCode(request.getCode())
                .orElseThrow(() -> new SecurityException("Invalid authorization code"));

        // Validate code expiration
        if (authCode.getExpiresAt().isBefore(Instant.now())) {
            authCodeRepository.delete(authCode);
            throw new SecurityException("Authorization code expired");
        }

        // Validate client
        if (!authCode.getClientId().equals(request.getClientId())) {
            throw new SecurityException("Client mismatch");
        }

        // Validate redirect_uri
        if (request.getRedirectUri() != null && !request.getRedirectUri().equals(authCode.getRedirectUri())) {
            throw new SecurityException("Redirect URI mismatch");
        }

        // PKCE validation if code_verifier present
        if (authCode.getCodeChallenge() != null) {
            validatePKCE(request.getCodeVerifier(), authCode.getCodeChallenge(), authCode.getCodeChallengeMethod());
        }

        // Generate tokens
        String userId = authCode.getUserId();
        String scope = authCode.getScope();
        
        OAuthAccessToken accessToken = createAccessToken(request.getClientId(), userId, scope);
        OAuthRefreshToken refreshToken = createRefreshToken(request.getClientId(), userId, scope);

        // Delete used authorization code
        authCodeRepository.delete(authCode);

        return TokenResponse.builder()
                .accessToken(accessToken.getToken())
                .tokenType("Bearer")
                .expiresIn(accessTokenTtl)
                .refreshToken(refreshToken.getToken())
                .scope(scope)
                .build();
    }

    /**
     * Refresh Token Grant
     * RFC 6749 - Section 6
     */
    private TokenResponse processRefreshTokenGrant(TokenRequest request) {
        if (request.getRefreshToken() == null) {
            throw new IllegalArgumentException("refresh_token is required");
        }

        OAuthRefreshToken refreshToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new SecurityException("Invalid refresh token"));

        // Validate expiration
        if (refreshToken.getExpiresAt().isBefore(Instant.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new SecurityException("Refresh token expired");
        }

        // Validate client
        if (!refreshToken.getClientId().equals(request.getClientId())) {
            throw new SecurityException("Client mismatch");
        }

        // Scope narrowing allowed
        String scope = request.getScope() != null ? request.getScope() : refreshToken.getScope();

        // Generate new access token
        OAuthAccessToken accessToken = createAccessToken(
                refreshToken.getClientId(),
                refreshToken.getUserId(),
                scope
        );

        return TokenResponse.builder()
                .accessToken(accessToken.getToken())
                .tokenType("Bearer")
                .expiresIn(accessTokenTtl)
                .refreshToken(refreshToken.getToken())
                .scope(scope)
                .build();
    }

    /**
     * Client Credentials Grant
     * RFC 6749 - Section 4.4
     */
    private TokenResponse processClientCredentialsGrant(TokenRequest request) {
        OAuthClient client = clientRepository.findByClientId(request.getClientId())
                .orElseThrow(() -> new SecurityException("Invalid client"));

        String scope = request.getScope() != null ? request.getScope() : client.getScope();

        // Generate access token (no refresh token for client credentials)
        OAuthAccessToken accessToken = createAccessToken(request.getClientId(), null, scope);

        return TokenResponse.builder()
                .accessToken(accessToken.getToken())
                .tokenType("Bearer")
                .expiresIn(accessTokenTtl)
                .scope(scope)
                .build();
    }

    /**
     * Resource Owner Password Credentials Grant
     * RFC 6749 - Section 4.3
     */
    private TokenResponse processPasswordGrant(TokenRequest request) {
        if (request.getUsername() == null || request.getPassword() == null) {
            throw new IllegalArgumentException("username and password are required");
        }

        // Authenticate user
        User user = userService.findByUsername(request.getUsername())
                .orElseThrow(() -> new SecurityException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new SecurityException("Invalid credentials");
        }

        String scope = request.getScope();
        OAuthAccessToken accessToken = createAccessToken(request.getClientId(), user.getId(), scope);
        OAuthRefreshToken refreshToken = createRefreshToken(request.getClientId(), user.getId(), scope);

        return TokenResponse.builder()
                .accessToken(accessToken.getToken())
                .tokenType("Bearer")
                .expiresIn(accessTokenTtl)
                .refreshToken(refreshToken.getToken())
                .scope(scope)
                .build();
    }

    /**
     * Device Authorization Grant
     * RFC 8628
     */
    private TokenResponse processDeviceCodeGrant(TokenRequest request) {
        if (request.getDeviceCode() == null) {
            throw new IllegalArgumentException("device_code is required");
        }

        // Implementation placeholder - device code flow requires additional state management
        throw new UnsupportedOperationException("Device code grant not yet implemented");
    }

    /**
     * Token Exchange
     * RFC 8693
     */
    private TokenResponse processTokenExchangeGrant(TokenRequest request) {
        // Implementation placeholder - token exchange for delegation/impersonation
        throw new UnsupportedOperationException("Token exchange not yet implemented");
    }

    /**
     * Initiate Device Flow
     * RFC 8628 - Section 3.1
     */
    public Map<String, Object> initiateDeviceFlow(String clientId, String scope) {
        OAuthClient client = clientRepository.findByClientId(clientId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid client"));

        String deviceCode = generateSecureToken();
        String userCode = generateUserCode();

        // Store device code (implementation needed)
        // ...

        return Map.of(
                "device_code", deviceCode,
                "user_code", userCode,
                "verification_uri", "https://auth.pragmia.io/device",
                "verification_uri_complete", "https://auth.pragmia.io/device?user_code=" + userCode,
                "expires_in", deviceCodeTtl,
                "interval", 5
        );
    }

    /**
     * Revoke Token
     * RFC 7009
     */
    @Transactional
    public void revokeToken(String token, String tokenTypeHint, String clientId) {
        if ("refresh_token".equals(tokenTypeHint)) {
            refreshTokenRepository.findByToken(token).ifPresent(refreshTokenRepository::delete);
        } else {
            accessTokenRepository.findByToken(token).ifPresent(accessTokenRepository::delete);
        }
    }

    /**
     * Introspect Token
     * RFC 7662
     */
    public Map<String, Object> introspectToken(String token, String tokenTypeHint, String clientId) {
        Optional<OAuthAccessToken> accessToken = accessTokenRepository.findByToken(token);
        
        if (accessToken.isPresent() && accessToken.get().getExpiresAt().isAfter(Instant.now())) {
            OAuthAccessToken at = accessToken.get();
            return Map.of(
                    "active", true,
                    "client_id", at.getClientId(),
                    "scope", at.getScope() != null ? at.getScope() : "",
                    "exp", at.getExpiresAt().getEpochSecond(),
                    "token_type", "Bearer"
            );
        }

        return Map.of("active", false);
    }

    // Helper methods

    private OAuthAccessToken createAccessToken(String clientId, String userId, String scope) {
        OAuthAccessToken accessToken = new OAuthAccessToken();
        accessToken.setToken(generateSecureToken());
        accessToken.setClientId(clientId);
        accessToken.setUserId(userId);
        accessToken.setScope(scope);
        accessToken.setIssuedAt(Instant.now());
        accessToken.setExpiresAt(Instant.now().plusSeconds(accessTokenTtl));
        return accessTokenRepository.save(accessToken);
    }

    private OAuthRefreshToken createRefreshToken(String clientId, String userId, String scope) {
        OAuthRefreshToken refreshToken = new OAuthRefreshToken();
        refreshToken.setToken(generateSecureToken());
        refreshToken.setClientId(clientId);
        refreshToken.setUserId(userId);
        refreshToken.setScope(scope);
        refreshToken.setIssuedAt(Instant.now());
        refreshToken.setExpiresAt(Instant.now().plusSeconds(refreshTokenTtl));
        return refreshTokenRepository.save(refreshToken);
    }

    private void validatePKCE(String codeVerifier, String codeChallenge, String codeChallengeMethod) {
        if (codeVerifier == null) {
            throw new SecurityException("code_verifier required for PKCE");
        }

        String computedChallenge;
        if ("S256".equals(codeChallengeMethod)) {
            computedChallenge = base64UrlEncode(sha256(codeVerifier));
        } else {
            computedChallenge = codeVerifier; // plain
        }

        if (!computedChallenge.equals(codeChallenge)) {
            throw new SecurityException("PKCE validation failed");
        }
    }

    private String generateSecureToken() {
        return UUID.randomUUID().toString().replace("-", "") +
               UUID.randomUUID().toString().replace("-", "");
    }

    private String generateUserCode() {
        Random random = new Random();
        return String.format("%04d-%04d", random.nextInt(10000), random.nextInt(10000));
    }

    private byte[] sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(input.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    private String base64UrlEncode(byte[] data) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(data);
    }
}
