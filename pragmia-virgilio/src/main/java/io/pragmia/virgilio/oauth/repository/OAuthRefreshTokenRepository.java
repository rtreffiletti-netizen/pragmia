
package io.pragmia.virgilio.oauth.repository;

import io.pragmia.virgilio.oauth.model.OAuthRefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OAuthRefreshTokenRepository extends JpaRepository<OAuthRefreshToken, UUID> {
    Optional<OAuthRefreshToken> findByTokenValue(String tokenValue);
    List<OAuthRefreshToken> findByUserId(UUID userId);
    List<OAuthRefreshToken> findByUserIdAndClientId(UUID userId, String clientId);

    @Modifying
    @Query("UPDATE OAuthRefreshToken t SET t.revoked = true, t.revokedAt = :now WHERE t.userId = :userId AND t.revoked = false")
    void revokeAllByUserId(UUID userId, Instant now);

    @Modifying
    @Query("DELETE FROM OAuthRefreshToken t WHERE t.expiresAt < :cutoff AND t.revoked = true")
    void deleteExpiredRevoked(Instant cutoff);
}
