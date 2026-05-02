package io.pragmia.virgilio.login;

import io.pragmia.api.audit.*;
import io.pragmia.kernel.audit.AuditEventPublisher;
import io.pragmia.virgilio.flow.FlowEngine;
import io.pragmia.virgilio.flow.FlowExecutionResult;
import io.pragmia.virgilio.flow.FlowNodeContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FlowAuthService {

    private final FlowEngine          flowEngine;
    private final ActiveFlowProvider  activeFlowProvider;
    private final AuditEventPublisher audit;

    public FlowExecutionResult authenticate(String username, String password,
                                             String totpCode, String sessionId,
                                             String clientId, String remoteIp, String userAgent) {
        String flowJson = activeFlowProvider.getActiveFlowJson()
            .orElseThrow(() -> new IllegalStateException("No active authentication flow configured"));

        FlowNodeContext ctx = new FlowNodeContext(sessionId, clientId, null, remoteIp, userAgent);
        ctx.setAttribute("username", username);
        ctx.setAttribute("password", password);
        if (totpCode != null && !totpCode.isBlank()) ctx.setAttribute("totp_code", totpCode);

        FlowExecutionResult result = flowEngine.execute(flowJson, ctx);

        AuditEventType eventType = result.isAllowed() ? AuditEventType.AUTH_SUCCESS : AuditEventType.AUTH_FAILURE;
        audit.publish(new AuditEvent(UUID.randomUUID().toString(), eventType, Instant.now(),
            result.getUserId(), null, sessionId, clientId, remoteIp, "LOGIN", "AUTHENTICATE",
            result.isAllowed() ? "SUCCESS" : "FAILURE",
            result.isDenied() ? result.denyReason() : null,
            Map.of("username", username != null ? username : "")));

        return result;
    }
}
