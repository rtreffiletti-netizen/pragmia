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
import java.util.List;
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

        List<Object> scopesList = jwt.getClaimAsStringList("scope") != null
            ? List.copyOf(jwt.getClaimAsStringList("scope")) : List.of();

        var policyRequest = new PolicyRequest(
            jwt.getSubject(),
            Map.of("claims", jwt.getClaims()),
            request.getRequestURI(),
            request.getMethod(),
            Map.of("remoteIp", request.getRemoteAddr(), "scopes", scopesList)
        );

        var decision = policyEvaluator.evaluate(policyRequest);

        if (decision != null && decision.effect() == PolicyDecision.Effect.DENY) {
            log.warn("[SOGLIA] DENY {} {} subject={}", request.getMethod(), request.getRequestURI(), jwt.getSubject());
            audit.publish(AuditEventType.POLICY_EVALUATED_DENY, jwt.getSubject(),
                null, null, request.getRemoteAddr(),
                request.getRequestURI(), request.getMethod(),
                "DENY", decision.reason(), Map.of("ip", request.getRemoteAddr()));
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied by policy");
            return;
        }

        chain.doFilter(request, response);
    }
}
