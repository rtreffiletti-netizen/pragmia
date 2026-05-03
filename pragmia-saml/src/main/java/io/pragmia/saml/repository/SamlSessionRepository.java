package io.pragmia.saml.repository;

import io.pragmia.saml.model.SamlSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface SamlSessionRepository extends JpaRepository<SamlSession, String> {
    List<SamlSession> findByUserIdAndActiveTrue(String userId);
    Optional<SamlSession> findBySessionIndexAndActiveTrue(String sessionIndex);

    @Modifying
    @Query("UPDATE SamlSession s SET s.active = false WHERE s.userId = :userId")
    void deactivateAllByUserId(String userId);

    @Modifying
    @Query("UPDATE SamlSession s SET s.active = false WHERE s.expiresAt < :now AND s.active = true")
    void expireOldSessions(Instant now);
}
