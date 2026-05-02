package io.pragmia.virgilio.mfa.repository;

import io.pragmia.virgilio.mfa.model.WebAuthnCredential;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WebAuthnCredentialRepository extends JpaRepository<WebAuthnCredential, UUID> {
    List<WebAuthnCredential> findByUserId(UUID userId);
    Optional<WebAuthnCredential> findByCredentialId(byte[] credentialId);
    void deleteByUserId(UUID userId);
}
