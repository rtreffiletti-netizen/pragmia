package io.pragmia.saml.repository;

import io.pragmia.saml.model.SamlAuditEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.Instant;

public interface SamlAuditEventRepository extends JpaRepository<SamlAuditEvent, String> {
    Page<SamlAuditEvent> findByUserId(String userId, Pageable pageable);
    Page<SamlAuditEvent> findBySpEntityId(String spEntityId, Pageable pageable);
    Page<SamlAuditEvent> findByTimestampBetween(Instant from, Instant to, Pageable pageable);
}
