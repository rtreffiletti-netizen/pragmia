package io.pragmia.kernel.audit;

import io.pragmia.api.audit.AuditEvent;
import io.pragmia.api.audit.AuditEventConsumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.List;

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

    public void publish(AuditEvent event) {
        spring.publishEvent(event);
        consumers.stream()
            .filter(c -> c.getSubscribedTypes().contains(event.eventType()))
            .forEach(c -> {
                try { c.onEvent(event); }
                catch (Exception e) { log.error("[Audit] consumer {} failed: {}", c.getName(), e.getMessage()); }
            });
    }
}
