package io.pragmia.api.audit;

import java.time.Instant;
import java.util.Map;

public record AuditEvent(
    String eventId,
    AuditEventType eventType,
    Instant timestamp,
    String userId,
    String adminId,
    String sessionId,
    String clientId,
    String remoteIp,
    String resource,
    String action,
    String result,
    String failureReason,
    Map<String, Object> metadata
) {}
