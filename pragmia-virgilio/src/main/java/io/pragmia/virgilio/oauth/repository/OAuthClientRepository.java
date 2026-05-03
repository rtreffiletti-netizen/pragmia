package io.pragmia.virgilio.oauth.repository;

import io.pragmia.virgilio.oauth.model.OAuthClient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OAuthClientRepository extends JpaRepository<OAuthClient, UUID> {
    Optional<OAuthClient> findByClientId(String clientId);
    Optional<OAuthClient> findByClientIdAndEnabledTrue(String clientId);
}
