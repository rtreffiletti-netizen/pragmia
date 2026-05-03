package io.pragmia.virgilio.oauth.service;

import io.pragmia.virgilio.oauth.model.*;
import io.pragmia.virgilio.oauth.repository.*;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Key;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenService {

    private final OAuthAccessTokenRepository accessTokenRepository;
    private final OAuthRefreshTokenRepository refreshTokenRepository;
    private final OAuthClientRepository clientRepository;
    private final Key jwtSigningKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Generate access token (JWT format)
     */
    @Transactional
    public OAuthAccessToken createAccessToken(UUID userId, String clientId, String scope) {
        OAuthClient client = clientRepository.findByClientId(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found"));

        String jwtToken = Jwts.builder()
                .setSubject(userId.toString())
                .setAudience(clientId)
                .setIssuer("pragmia-virgilio")
                .setIssuedAt(Date.from(Instant.now()))
                .setExpiration(Date.from(Instant.now().plusSeconds(client.getAccessTokenValidity())))
                .claim("scope", scope)
                .claim("client_id", clientId)
                .signWith(jwtSigningKey)
                .compact();

        OAuthAccessToken accessToken = OAuthAccessToken.builder()
                .tokenValue(jwtToken)
                .tokenType("Bearer")
                .userId(userId)
                .clientId(clientId)
                .scope(scope)
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(client.getAccessTokenValidity()))
                .audience(clientId)
                .issuer("pragmia-virgilio")
                .build();

        return accessTokenRepository.save(accessToken);
    }

    /**
     * Generate refresh token (opaque)
     */
    @Transactional
    public OAuthRefreshToken createRefreshToken(UUID userId, String clientId, String scope) {
        OAuthClient client = clientRepository.findByClientId(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found"));

        String refreshTokenValue = generateSecureToken(64);

        OAuthRefreshToken refreshToken = OAuthRefreshToken.builder()
                .tokenValue(refreshTokenValue)
                .userId(userId)
                .clientId(clientId)
                .scope(scope)
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(client.getRefreshTokenValidity()))
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    /**
     * Validate and retrieve access token
     */
    public Optional<OAuthAccessToken> validateAccessToken(String tokenValue) {
        return accessTokenRepository.findByTokenValue(tokenValue)
                .filter(OAuthAccessToken::isValid);
    }

    /**
     * Validate and retrieve refresh token
     */
    public Optional<OAuthRefreshToken> validateRefreshToken(String tokenValue) {
        return refreshTokenRepository.findByTokenValue(tokenValue)
                .filter(OAuthRefreshToken::isValid);
    }

    /**
     * Refresh access token using refresh token
     */
    @Transactional
    public OAuthAccessToken refreshAccessToken(String refreshTokenValue) {
        OAuthRefreshToken refreshToken = validateRefreshToken(refreshTokenValue)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        // Create new access token
        return createAccessToken(refreshToken.getUserId(), 
                               refreshToken.getClientId(), 
                               refreshToken.getScope());
    }

    /**
     * Revoke access token
     */
    @Transactional
    public void revokeAccessToken(String tokenValue) {
        accessTokenRepository.findByTokenValue(tokenValue).ifPresent(token -> {
            token.setRevoked(true);
            token.setRevokedAt(Instant.now());
            accessTokenRepository.save(token);
            log.info("Access token revoked for user {}", token.getUserId());
        });
    }

    /**
     * Revoke refresh token
     */
    @Transactional
    public void revokeRefreshToken(String tokenValue) {
        refreshTokenRepository.findByTokenValue(tokenValue).ifPresent(token -> {
            token.setRevoked(true);
            token.setRevokedAt(Instant.now());
            refreshTokenRepository.save(token);
            log.info("Refresh token revoked for user {}", token.getUserId());
        });
    }

    /**
     * Generate cryptographically secure random token
     */
    private String generateSecureToken(int length) {
        byte[] randomBytes = new byte[length];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    /**
     * Introspect token (OAuth2 introspection endpoint support)
     */
    public Map<String, Object> introspectToken(String token) {
        Map<String, Object> response = new HashMap<>();
        
        Optional<OAuthAccessToken> accessToken = validateAccessToken(token);
        if (accessToken.isPresent()) {
            OAuthAccessToken at = accessToken.get();
            response.put("active", true);
            response.put("scope", at.getScope());
            response.put("client_id", at.getClientId());
            response.put("username", at.getUserId().toString());
            response.put("token_type", at.getTokenType());
            response.put("exp", at.getExpiresAt().getEpochSecond());
            response.put("iat", at.getIssuedAt().getEpochSecond());
            return response;
        }

        response.put("active", false);
        return response;
    }
}
