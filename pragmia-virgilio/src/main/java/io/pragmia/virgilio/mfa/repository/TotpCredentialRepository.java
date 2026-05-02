package io.pragmia.virgilio.mfa.repository;

import io.pragmia.virgilio.mfa.model.TotpCredential;
import io.pragmia.virgilio.user.model.VirgilioUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TotpCredentialRepository extends JpaRepository<TotpCredential, UUID> {
    Optional<TotpCredential> findByUser(VirgilioUser user);
    Optional<TotpCredential> findByUserId(UUID userId);
    boolean existsByUser(VirgilioUser user);
}
