package io.pragmia.clio.consumer;

import io.pragmia.api.audit.AuditEvent;
import io.pragmia.api.audit.AuditEventConsumer;
import io.pragmia.api.audit.AuditEventType;
import io.pragmia.clio.model.AuditRecord;
import io.pragmia.clio.repository.AuditRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ClioAuditConsumer implements AuditEventConsumer {

    private final AuditRecordRepository repo;

    @Override
    public String getName() { return "clio-db-consumer"; }

    @Override
    public List<AuditEventType> getSubscribedTypes() { return List.of(AuditEventType.values()); }

    @Override
    public void onEvent(AuditEvent event) {
        try {
            var record = AuditRecord.builder()
                .eventId(event.eventId())
                .eventType(event.eventType())
                .ts(event.timestamp())
                .userId(event.userId())
                .adminId(event.adminId())
                .sessionId(event.sessionId())
                .clientId(event.clientId())
                .remoteIp(event.remoteIp())
                .resource(event.resource())
                .action(event.action())
                .result(event.result())
                .failureReason(event.failureReason())
                .metadata(event.metadata())
                .build();
            repo.save(record);
        } catch (Exception ex) {
            log.error("[CLIO] Failed to persist audit event {}: {}", event.eventId(), ex.getMessage());
        }
    }
}
