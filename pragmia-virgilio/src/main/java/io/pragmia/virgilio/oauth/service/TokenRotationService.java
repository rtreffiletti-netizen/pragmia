
package io.pragmia.virgilio.oauth.service;

import io.pragmia.virgilio.oauth.model.OAuthAccessToken;
import io.pragmia.virgilio.oauth.model.OAuthRefreshToken;
import io.pragmia.virgilio.oauth.model.TokenResponse;
import io.pragmia.virgilio.oauth.repository.OAuthAccessTokenRepository;
import io.pragmia.virgilio.oauth.repository.OAuthRefreshTokenRepository;
import io.pragmia.virgilio.security.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Refresh Token Rotation — RFC 6749 §10.4 best practice.
 * Ad ogni uso del refresh token:
 *  1. Il vecchio refresh token viene revocato immediatamente
 *  2. Viene emesso un nuovo refresh token (rotation)
 *  3. Viene emesso un nuovo access token
 * Questo impedisce il riuso di refresh token rubati (replay attack).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TokenRotationService {

    @Value("${pragmia.oauth.access-token-ttl:900}")
    private long accessTokenTtlSeconds;

    @Value("${pragmia.oauth.refresh-token-ttl:86400}")
    private long refreshTokenTtlSeconds;

    @Value("${pragmia.oauth.refresh-token-rotation:true}")
    private boolean rotationEnabled;

    private final OAuthAccessTokenRepository accessTokenRepository;
    private final OAuthRefreshTokenRepository refreshTokenRepository;
    private final TokenRevocationService revocationService;
    private final JwtService jwtService;
    private final StringRedisTemplate redisTemplate;

    /**
     * Esegue la rotation del refresh token.
     * Chiamato da TokenService nel grant type "refresh_token".
     *
     * @param oldRefreshToken il refresh token corrente (già validato)
     * @param clientId        client che effettua la richiesta
     * @return nuova coppia access + refresh token
     */
    @Transactional
    public TokenResponse rotate(OAuthRefreshToken oldRefreshToken, String clientId) {
        Instant now = Instant.now();

        // 1. Revoca il vecchio refresh token
        oldRefreshToken.setRevoked(true);
        oldRefreshToken.setRevokedAt(now);
        refreshTokenRepository.save(oldRefreshToken);
        log.debug("[ROTATION] Vecchio refresh token revocato: userId={}", oldRefreshToken.getUserId());

        // 2. Genera nuovo access token JWT
        String newAccessTokenValue = jwtService.generateAccessToken(
            oldRefreshToken.getUserId().toString(),
            oldRefreshToken.getClientId(),
            oldRefreshToken.getScope()
        );

        OAuthAccessToken newAccessToken = OAuthAccessToken.builder()
            .tokenValue(newAccessTokenValue)
            .userId(oldRefreshToken.getUserId())
            .clientId(oldRefreshToken.getClientId())
            .scope(oldRefreshToken.getScope())
            .issuedAt(now)
            .expiresAt(now.plusSeconds(accessTokenTtlSeconds))
            .revoked(false)
            .build();
        accessTokenRepository.save(newAccessToken);

        // 3. Genera nuovo refresh token (se rotation abilitata)
        String newRefreshTokenValue = UUID.randomUUID().toString();
        OAuthRefreshToken newRefreshToken = OAuthRefreshToken.builder()
            .tokenValue(newRefreshTokenValue)
            .userId(oldRefreshToken.getUserId())
            .clientId(oldRefreshToken.getClientId())
            .scope(oldRefreshToken.getScope())
            .issuedAt(now)
            .expiresAt(now.plusSeconds(refreshTokenTtlSeconds))
            .accessTokenId(newAccessToken.getId())
            .revoked(false)
            .build();
        refreshTokenRepository.save(newRefreshToken);

        log.info("[ROTATION] Token rotati per userId={}", oldRefreshToken.getUserId());

        return TokenResponse.builder()
            .accessToken(newAccessTokenValue)
            .tokenType("Bearer")
            .expiresIn(accessTokenTtlSeconds)
            .refreshToken(newRefreshTokenValue)
            .scope(oldRefreshToken.getScope())
            .build();
    }
}
