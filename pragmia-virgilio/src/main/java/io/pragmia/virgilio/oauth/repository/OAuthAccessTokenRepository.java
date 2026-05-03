package io.pragmia.virgilio.oauth.repository;

import io.pragmia.virgilio.oauth.model.OAuthAccessToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OAuthAccessTokenRepository extends JpaRepository<OAuthAccessToken, UUID> {
    Optional<OAuthAccessToken> findByTokenValue(String tokenValue);
    List<OAuthAccessToken> findByUserId(UUID userId);
    List<OAuthAccessToken> findByClientId(String clientId);
    List<OAuthAccessToken> findByUserIdAndClientId(UUID userId, String clientId);
}
