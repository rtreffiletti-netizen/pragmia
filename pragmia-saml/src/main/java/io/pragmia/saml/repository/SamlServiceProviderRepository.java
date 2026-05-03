package io.pragmia.saml.repository;

import io.pragmia.saml.model.SamlServiceProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface SamlServiceProviderRepository extends JpaRepository<SamlServiceProvider, String> {
    Optional<SamlServiceProvider> findByEntityId(String entityId);
    List<SamlServiceProvider> findByEnabledTrue();
}
