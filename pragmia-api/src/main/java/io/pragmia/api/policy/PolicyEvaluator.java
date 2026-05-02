package io.pragmia.api.policy;

public interface PolicyEvaluator {
    String getName();
    int getPriority();
    PolicyDecision evaluate(PolicyRequest request);
}
