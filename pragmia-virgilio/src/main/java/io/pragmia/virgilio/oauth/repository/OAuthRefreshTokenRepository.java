package io.pragmia.virgilio.oauth.repository;

import io.pragmia.virgilio.oauth.model.OAuthRefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OAuthRefreshTokenRepository extends JpaRepository<OAuthRefreshToken, UUID> {
    Optional<OAuthRefreshToken> findByTokenValue(String tokenValue);
    List<OAuthRefreshToken> findByUserId(UUID userId);
}
