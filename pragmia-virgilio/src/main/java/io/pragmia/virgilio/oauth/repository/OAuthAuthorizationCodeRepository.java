package io.pragmia.virgilio.oauth.repository;

import io.pragmia.virgilio.oauth.model.OAuthAuthorizationCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OAuthAuthorizationCodeRepository extends JpaRepository<OAuthAuthorizationCode, UUID> {
    Optional<OAuthAuthorizationCode> findByCode(String code);
}
