package io.pragmia.virgilio.session.repository;

import io.pragmia.virgilio.session.model.SessionStatus;
import io.pragmia.virgilio.session.model.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SessionRepository extends JpaRepository<UserSession, UUID> {

    Optional<UserSession> findBySessionToken(String sessionToken);

    List<UserSession> findByUserId(UUID userId);

    List<UserSession> findByUserIdAndActiveTrue(UUID userId);

    List<UserSession> findByStatusAndExpiresAtBefore(SessionStatus status, Instant instant);

    @Modifying
    @Query("UPDATE UserSession s SET s.active = false, s.status = 'REVOKED' WHERE s.userId = :userId AND s.id != :currentSessionId")
    int revokeOtherUserSessions(UUID userId, UUID currentSessionId);

    @Modifying
    @Query("UPDATE UserSession s SET s.active = false, s.status = 'EXPIRED' WHERE s.expiresAt < :now AND s.active = true")
    int markExpiredSessions(Instant now);

    @Query("SELECT COUNT(s) FROM UserSession s WHERE s.userId = :userId AND s.active = true AND s.status = 'ACTIVE'")
    long countActiveSessions(UUID userId);
}
