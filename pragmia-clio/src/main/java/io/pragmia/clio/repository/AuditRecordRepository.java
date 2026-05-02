package io.pragmia.clio.repository;

import io.pragmia.api.audit.AuditEventType;
import io.pragmia.clio.model.AuditRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
public interface AuditRecordRepository extends JpaRepository<AuditRecord, String> {
    Page<AuditRecord> findByUserId(String userId, Pageable pageable);
    Page<AuditRecord> findByEventType(AuditEventType type, Pageable pageable);
    Page<AuditRecord> findByTsBetween(Instant from, Instant to, Pageable pageable);
    Page<AuditRecord> findBySessionId(String sessionId, Pageable pageable);
}
