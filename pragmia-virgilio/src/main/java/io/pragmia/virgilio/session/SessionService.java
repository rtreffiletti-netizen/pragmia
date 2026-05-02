package io.pragmia.virgilio.session;

import io.pragmia.api.audit.*;
import io.pragmia.kernel.audit.AuditEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionService {

    private static final String SESSION_KEY_PATTERN = "spring:session:sessions:*";

    private final FindByIndexNameSessionRepository<? extends Session> sessionRepo;
    private final RedisTemplate<String, Object> redis;
    private final AuditEventPublisher audit;

    public boolean killSession(String sessionId, UUID adminId) {
        try {
            sessionRepo.deleteById(sessionId);
            publishKill(AuditEventType.SESSION_KILLED_BY_ADMIN, sessionId, adminId, 1);
            return true;
        } catch (Exception e) { log.error("killSession error: {}", e.getMessage()); return false; }
    }

    public int killSessionsByUser(String principalName, UUID adminId) {
        Map<String, ? extends Session> sessions = sessionRepo.findByPrincipalName(principalName);
        sessions.keySet().forEach(sessionRepo::deleteById);
        publishKill(AuditEventType.SESSION_KILLED_BY_ADMIN, "user:" + principalName, adminId, sessions.size());
        return sessions.size();
    }

    public int killAllSessions(UUID adminId) {
        Set<String> keys = redis.keys(SESSION_KEY_PATTERN);
        int count = keys != null ? keys.size() : 0;
        if (count > 0) redis.delete(keys);
        publishKill(AuditEventType.SESSION_KILL_BULK, "ALL", adminId, count);
        log.warn("[SOGLIA] {} sessions killed by admin {}", count, adminId);
        return count;
    }

    public long countActiveSessions() {
        Set<String> keys = redis.keys(SESSION_KEY_PATTERN);
        return keys != null ? keys.size() : 0L;
    }

    private void publishKill(AuditEventType type, String resource, UUID adminId, int count) {
        audit.publish(new AuditEvent(UUID.randomUUID().toString(), type, Instant.now(),
            null, adminId != null ? adminId.toString() : null,
            null, null, null, resource, "KILL_SESSION", "SUCCESS", null,
            Map.of("count", count)));
    }
}
