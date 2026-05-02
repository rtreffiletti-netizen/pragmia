package io.pragmia.kernel.audit;

import io.pragmia.api.audit.AuditEvent;
import io.pragmia.api.audit.AuditEventConsumer;
import io.pragmia.api.audit.AuditEventType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
public class AuditEventPublisher {

    private final ApplicationEventPublisher spring;
    private final List<AuditEventConsumer> consumers;

    public AuditEventPublisher(ApplicationEventPublisher spring, List<AuditEventConsumer> consumers) {
        this.spring = spring;
        this.consumers = consumers;
        log.info("[AuditPublisher] {} consumer(s) registered", consumers.size());
    }

    /** Metodo principale — accetta AuditEvent già costruito */
    public void publish(AuditEvent event) {
        spring.publishEvent(event);
        consumers.stream()
            .filter(c -> c.getSubscribedTypes().contains(event.eventType()))
            .forEach(c -> {
                try { c.onEvent(event); }
                catch (Exception e) { log.error("[Audit] consumer {} failed: {}", c.getName(), e.getMessage()); }
            });
    }

    /** Metodo convenienza — costruisce l'AuditEvent dai parametri singoli */
    public void publish(AuditEventType type, String userId, String sessionId,
                        String clientId, String remoteIp,
                        String resourceType, String action, String outcome,
                        String failureReason, Map<String, ?> details) {
        publish(new AuditEvent(
            UUID.randomUUID().toString(), type, Instant.now(),
            userId, null, sessionId, clientId, remoteIp,
            resourceType, action, outcome, failureReason,
            details != null ? (Map<String,Object>) details : Map.of()
        ));
    }
}
