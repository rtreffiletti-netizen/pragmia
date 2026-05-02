package io.pragmia.kernel.policy;

import io.pragmia.api.policy.PolicyDecision;
import io.pragmia.api.policy.PolicyEvaluator;
import io.pragmia.api.policy.PolicyRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Slf4j
@Component
public class PolicyEvaluatorComposite {

    private final List<PolicyEvaluator> evaluators;

    public PolicyEvaluatorComposite(List<PolicyEvaluator> evaluators) {
        this.evaluators = evaluators.stream()
            .sorted(Comparator.comparingInt(PolicyEvaluator::getPriority))
            .toList();
        log.info("[PolicyComposite] {} evaluator(s)", this.evaluators.size());
    }

    public PolicyDecision evaluate(PolicyRequest request) {
        for (PolicyEvaluator ev : evaluators) {
            PolicyDecision d = ev.evaluate(request);
            if (d.effect() != PolicyDecision.Effect.NOT_APPLICABLE) return d;
        }
        return PolicyDecision.deny("No applicable policy — fail secure", "composite");
    }
}
