
package io.pragmia.virgilio.oauth.service;

import io.pragmia.virgilio.oauth.repository.OAuthAccessTokenRepository;
import io.pragmia.virgilio.oauth.repository.OAuthRefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Pulizia periodica dei token scaduti e revocati dal DB.
 * Eseguito ogni notte alle 03:00 per non impattare i picchi di traffico.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TokenCleanupScheduler {

    private final OAuthAccessTokenRepository accessTokenRepository;
    private final OAuthRefreshTokenRepository refreshTokenRepository;

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void cleanupExpiredTokens() {
        Instant cutoff = Instant.now().minus(1, ChronoUnit.DAYS);
        accessTokenRepository.deleteExpiredRevoked(cutoff);
        refreshTokenRepository.deleteExpiredRevoked(cutoff);
        log.info("[CLEANUP] Token scaduti e revocati rimossi dal DB (cutoff={})", cutoff);
    }
}
