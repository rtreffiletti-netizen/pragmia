package io.pragmia.minos.engine;

import io.pragmia.api.policy.PolicyDecision;
import io.pragmia.api.policy.PolicyEvaluator;
import io.pragmia.api.policy.PolicyRequest;
import io.pragmia.minos.model.Policy;
import io.pragmia.minos.model.PolicyEffect;
import io.pragmia.minos.repository.PolicyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class SpelPolicyEvaluator implements PolicyEvaluator {

    private final PolicyRepository repo;
    private final ExpressionParser parser = new SpelExpressionParser();

    @Override
    public PolicyDecision evaluate(PolicyRequest request) {
        List<Policy> policies = repo.findByActiveTrueOrderByPriorityAsc();
        for (Policy policy : policies) {
            try {
                var ctx = new StandardEvaluationContext();
                ctx.setVariable("subject",     request.getSubject());
                ctx.setVariable("resource",    request.getResource());
                ctx.setVariable("action",      request.getAction());
                ctx.setVariable("environment", request.getEnvironment());
                ctx.setVariable("claims",      request.getClaims());

                Boolean match = parser.parseExpression(policy.getCondition())
                                      .getValue(ctx, Boolean.class);
                if (Boolean.TRUE.equals(match)) {
                    log.debug("[MINOS] Policy '{}' matched → {}", policy.getName(), policy.getEffect());
                    return policy.getEffect() == PolicyEffect.PERMIT
                        ? PolicyDecision.PERMIT
                        : PolicyDecision.DENY;
                }
            } catch (Exception ex) {
                log.warn("[MINOS] Error evaluating policy '{}': {}", policy.getName(), ex.getMessage());
            }
        }
        return PolicyDecision.PERMIT; // default permissive se nessuna policy corrisponde
    }

    @Override
    public String getName() { return "minos-spel-evaluator"; }

    @Override
    public int getPriority() { return 10; }
}
