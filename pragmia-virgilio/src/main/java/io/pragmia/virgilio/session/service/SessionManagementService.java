package io.pragmia.virgilio.session.service;

import io.pragmia.virgilio.session.model.SessionStatus;
import io.pragmia.virgilio.session.model.UserSession;
import io.pragmia.virgilio.session.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SessionManagementService {

    private final SessionRepository sessionRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    private static final long DEFAULT_SESSION_TIMEOUT_SECONDS = 3600; // 1 hour
    private static final int SESSION_TOKEN_LENGTH = 64;

    /**
     * Create a new user session with hijacking protection
     */
    @Transactional
    public UserSession createSession(UUID userId, String ipAddress, String userAgent, 
                                      String deviceFingerprint, String geolocation) {
        // Generate cryptographically secure session token
        String sessionToken = generateSecureToken();

        UserSession session = UserSession.builder()
                .sessionToken(sessionToken)
                .userId(userId)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .deviceFingerprint(deviceFingerprint)
                .geolocation(geolocation)
                .createdAt(Instant.now())
                .lastAccessedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(DEFAULT_SESSION_TIMEOUT_SECONDS))
                .active(true)
                .status(SessionStatus.ACTIVE)
                .build();

        log.info("Creating new session for user {} from IP {}", userId, ipAddress);
        return sessionRepository.save(session);
    }

    /**
     * Validate session with hijacking detection
     */
    @Transactional
    public Optional<UserSession> validateSession(String sessionToken, String currentIpAddress, 
                                                   String currentUserAgent, String currentDeviceFingerprint) {
        Optional<UserSession> sessionOpt = sessionRepository.findBySessionToken(sessionToken);

        if (sessionOpt.isEmpty()) {
            log.warn("Session token not found: {}", sessionToken);
            return Optional.empty();
        }

        UserSession session = sessionOpt.get();

        // Check if session is valid
        if (!session.isValid()) {
            log.warn("Invalid session {} for user {}", session.getId(), session.getUserId());
            return Optional.empty();
        }

        // Session hijacking detection
        boolean hijackingSuspected = detectHijacking(session, currentIpAddress, 
                                                       currentUserAgent, currentDeviceFingerprint);

        if (hijackingSuspected) {
            log.error("Session hijacking suspected for session {} - IP changed from {} to {}", 
                     session.getId(), session.getIpAddress(), currentIpAddress);
            session.setStatus(SessionStatus.SUSPICIOUS);
            session.setActive(false);
            sessionRepository.save(session);
            return Optional.empty();
        }

        // Update last accessed time
        session.setLastAccessedAt(Instant.now());
        sessionRepository.save(session);

        return Optional.of(session);
    }

    /**
     * Detect potential session hijacking
     */
    private boolean detectHijacking(UserSession session, String currentIpAddress, 
                                     String currentUserAgent, String currentDeviceFingerprint) {
        // IP address change detection
        if (!session.getIpAddress().equals(currentIpAddress)) {
            log.warn("IP address mismatch: session={}, current={}", 
                    session.getIpAddress(), currentIpAddress);
            return true;
        }

        // User-Agent change detection
        if (session.getUserAgent() != null && !session.getUserAgent().equals(currentUserAgent)) {
            log.warn("User-Agent mismatch for session {}", session.getId());
            return true;
        }

        // Device fingerprint change detection
        if (session.getDeviceFingerprint() != null && currentDeviceFingerprint != null 
            && !session.getDeviceFingerprint().equals(currentDeviceFingerprint)) {
            log.warn("Device fingerprint mismatch for session {}", session.getId());
            return true;
        }

        return false;
    }

    /**
     * Revoke a specific session
     */
    @Transactional
    public void revokeSession(String sessionToken) {
        sessionRepository.findBySessionToken(sessionToken).ifPresent(session -> {
            session.setActive(false);
            session.setStatus(SessionStatus.REVOKED);
            sessionRepository.save(session);
            log.info("Session {} revoked for user {}", session.getId(), session.getUserId());
        });
    }

    /**
     * Revoke all sessions for a user except the current one
     */
    @Transactional
    public int revokeOtherSessions(UUID userId, UUID currentSessionId) {
        int revokedCount = sessionRepository.revokeOtherUserSessions(userId, currentSessionId);
        log.info("Revoked {} sessions for user {}", revokedCount, userId);
        return revokedCount;
    }

    /**
     * Get all active sessions for a user
     */
    public List<UserSession> getActiveSessions(UUID userId) {
        return sessionRepository.findByUserIdAndActiveTrue(userId);
    }

    /**
     * Get session count for a user
     */
    public long getActiveSessionCount(UUID userId) {
        return sessionRepository.countActiveSessions(userId);
    }

    /**
     * Scheduled task to cleanup expired sessions
     */
    @Scheduled(fixedRate = 300000) // Every 5 minutes
    @Transactional
    public void cleanupExpiredSessions() {
        int expiredCount = sessionRepository.markExpiredSessions(Instant.now());
        if (expiredCount > 0) {
            log.info("Marked {} expired sessions", expiredCount);
        }
    }

    /**
     * Generate cryptographically secure session token
     */
    private String generateSecureToken() {
        byte[] randomBytes = new byte[SESSION_TOKEN_LENGTH];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    /**
     * Extend session timeout
     */
    @Transactional
    public void extendSession(String sessionToken, long additionalSeconds) {
        sessionRepository.findBySessionToken(sessionToken).ifPresent(session -> {
            session.setExpiresAt(session.getExpiresAt().plusSeconds(additionalSeconds));
            sessionRepository.save(session);
            log.debug("Extended session {} by {} seconds", session.getId(), additionalSeconds);
        });
    }
}
