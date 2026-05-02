package io.pragmia.api.audit;

import java.util.List;

public interface AuditEventConsumer {
    String getName();
    List<AuditEventType> getSubscribedTypes();
    void onEvent(AuditEvent event);
}
