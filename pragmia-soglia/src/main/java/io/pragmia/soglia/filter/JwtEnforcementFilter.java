package io.pragmia.soglia.filter;

import io.pragmia.api.audit.AuditEventType;
import io.pragmia.api.policy.PolicyDecision;
import io.pragmia.api.policy.PolicyRequest;
import io.pragmia.kernel.audit.AuditEventPublisher;
import io.pragmia.kernel.policy.PolicyEvaluatorComposite;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtEnforcementFilter extends OncePerRequestFilter {

    private final PolicyEvaluatorComposite policyEvaluator;
    private final AuditEventPublisher audit;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof Jwt jwt)) {
            chain.doFilter(request, response);
            return;
        }

        var policyRequest = PolicyRequest.builder()
            .subject(jwt.getSubject())
            .resource(request.getRequestURI())
            .action(request.getMethod())
            .environment(Map.of(
                "remoteIp", request.getRemoteAddr(),
                "scopes",   jwt.getClaimAsStringList("scope") != null
                                ? jwt.getClaimAsStringList("scope") : java.util.List.of()
            ))
            .claims(jwt.getClaims())
            .build();

        var decision = policyEvaluator.evaluate(policyRequest);

        if (decision == PolicyDecision.DENY) {
            log.warn("[SOGLIA] DENY {} {} subject={}", request.getMethod(), request.getRequestURI(), jwt.getSubject());
            audit.publish(AuditEventType.POLICY_EVALUATED_DENY, jwt.getSubject(),
                null, null, null, request.getRequestURI(), request.getMethod(),
                "DENY", "policy deny", Map.of("ip", request.getRemoteAddr()));
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied by policy");
            return;
        }

        chain.doFilter(request, response);
    }
}
