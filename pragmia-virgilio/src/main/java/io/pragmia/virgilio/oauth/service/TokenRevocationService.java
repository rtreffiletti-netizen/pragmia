
package io.pragmia.virgilio.oauth.service;

import io.pragmia.virgilio.oauth.model.OAuthAccessToken;
import io.pragmia.virgilio.oauth.model.OAuthRefreshToken;
import io.pragmia.virgilio.oauth.repository.OAuthAccessTokenRepository;
import io.pragmia.virgilio.oauth.repository.OAuthRefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Token Revocation Service — RFC 7009
 * Gestisce la revoca di access token e refresh token.
 * La blacklist è mantenuta su Redis per lookup O(1) ad ogni validazione.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TokenRevocationService {

    private static final String BLACKLIST_PREFIX = "pragmia:token:blacklist:";

    private final OAuthAccessTokenRepository accessTokenRepository;
    private final OAuthRefreshTokenRepository refreshTokenRepository;
    private final StringRedisTemplate redisTemplate;

    /**
     * Revoca un token (access o refresh) — RFC 7009 Section 2.1.
     * Se il token è un refresh token, revoca anche tutti gli access token associati.
     *
     * @param tokenValue   valore del token da revocare
     * @param tokenTypeHint "access_token" | "refresh_token" | null
     * @param clientId     client che richiede la revoca (deve coincidere con il client del token)
     */
    @Transactional
    public void revokeToken(String tokenValue, String tokenTypeHint, String clientId) {
        // Prova prima il tipo suggerito, poi l'altro (RFC 7009 §2.1)
        boolean revoked = false;

        if (!"access_token".equals(tokenTypeHint)) {
            revoked = tryRevokeRefreshToken(tokenValue, clientId);
        }
        if (!revoked && !"refresh_token".equals(tokenTypeHint)) {
            revoked = tryRevokeAccessToken(tokenValue, clientId);
        }
        // Se il tipo hint era sbagliato, prova comunque l'altro
        if (!revoked && "access_token".equals(tokenTypeHint)) {
            revoked = tryRevokeRefreshToken(tokenValue, clientId);
        }
        if (!revoked && "refresh_token".equals(tokenTypeHint)) {
            revoked = tryRevokeAccessToken(tokenValue, clientId);
        }

        // RFC 7009: risponde sempre 200 OK anche se il token non esiste
        if (!revoked) {
            log.debug("[REVOCATION] Token non trovato o già revocato — risposta 200 OK per sicurezza");
        }
    }

    /**
     * Verifica se un access token è nella blacklist Redis.
     * Chiamato da JwtService ad ogni validazione.
     */
    public boolean isBlacklisted(String tokenValue) {
        String key = BLACKLIST_PREFIX + tokenValue;
        Boolean exists = redisTemplate.hasKey(key);
        return Boolean.TRUE.equals(exists);
    }

    /**
     * Revoca tutti i token (access + refresh) di un utente su un client.
     * Usato da SessionManagementService per il Session Kill.
     */
    @Transactional
    public void revokeAllUserTokens(UUID userId, String clientId) {
        accessTokenRepository.findByUserIdAndClientId(userId, clientId).forEach(t -> {
            markRevoked(t);
            addToBlacklist(t.getTokenValue(), t.getExpiresAt());
        });
        refreshTokenRepository.findByUserId(userId).stream()
            .filter(t -> clientId.equals(t.getClientId()))
            .forEach(t -> markRevokedRefresh(t));
        log.info("[REVOCATION] Tutti i token revocati per userId={} clientId={}", userId, clientId);
    }

    /**
     * Revoca tutti i token di un utente su tutti i client.
     * Usato per disabilitazione account o compromissione credenziali.
     */
    @Transactional
    public void revokeAllUserTokensAllClients(UUID userId) {
        accessTokenRepository.findByUserId(userId).forEach(t -> {
            markRevoked(t);
            addToBlacklist(t.getTokenValue(), t.getExpiresAt());
        });
        refreshTokenRepository.findByUserId(userId).forEach(this::markRevokedRefresh);
        log.warn("[REVOCATION] TUTTI i token revocati per userId={}", userId);
    }

    // ── private helpers ───────────────────────────────────────────────────

    private boolean tryRevokeAccessToken(String tokenValue, String clientId) {
        Optional<OAuthAccessToken> opt = accessTokenRepository.findByTokenValue(tokenValue);
        if (opt.isEmpty()) return false;
        OAuthAccessToken token = opt.get();
        if (!token.getClientId().equals(clientId)) {
            log.warn("[REVOCATION] clientId mismatch per access token — rifiutato");
            return false;
        }
        markRevoked(token);
        addToBlacklist(tokenValue, token.getExpiresAt());
        log.info("[REVOCATION] Access token revocato per userId={}", token.getUserId());
        return true;
    }

    private boolean tryRevokeRefreshToken(String tokenValue, String clientId) {
        Optional<OAuthRefreshToken> opt = refreshTokenRepository.findByTokenValue(tokenValue);
        if (opt.isEmpty()) return false;
        OAuthRefreshToken token = opt.get();
        if (!token.getClientId().equals(clientId)) {
            log.warn("[REVOCATION] clientId mismatch per refresh token — rifiutato");
            return false;
        }
        markRevokedRefresh(token);
        // Revoca anche l'access token collegato (se ancora valido)
        if (token.getAccessTokenId() != null) {
            accessTokenRepository.findById(token.getAccessTokenId()).ifPresent(at -> {
                markRevoked(at);
                addToBlacklist(at.getTokenValue(), at.getExpiresAt());
            });
        }
        log.info("[REVOCATION] Refresh token revocato per userId={}", token.getUserId());
        return true;
    }

    private void markRevoked(OAuthAccessToken token) {
        token.setRevoked(true);
        token.setRevokedAt(Instant.now());
        accessTokenRepository.save(token);
    }

    private void markRevokedRefresh(OAuthRefreshToken token) {
        token.setRevoked(true);
        token.setRevokedAt(Instant.now());
        refreshTokenRepository.save(token);
    }

    /**
     * Aggiunge il token alla blacklist Redis con TTL = tempo residuo alla scadenza.
     * Dopo la scadenza Redis rimuove la chiave automaticamente.
     */
    private void addToBlacklist(String tokenValue, Instant expiresAt) {
        if (expiresAt == null) return;
        Duration ttl = Duration.between(Instant.now(), expiresAt);
        if (ttl.isNegative() || ttl.isZero()) return;
        redisTemplate.opsForValue().set(BLACKLIST_PREFIX + tokenValue, "revoked", ttl);
    }
}
