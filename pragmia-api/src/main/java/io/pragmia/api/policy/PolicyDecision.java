package io.pragmia.api.policy;

public record PolicyDecision(Effect effect, String reason, String evaluatorName) {

    public enum Effect { PERMIT, DENY, NOT_APPLICABLE }

    public static PolicyDecision permit(String evaluator) {
        return new PolicyDecision(Effect.PERMIT, null, evaluator);
    }
    public static PolicyDecision deny(String reason, String evaluator) {
        return new PolicyDecision(Effect.DENY, reason, evaluator);
    }
    public static PolicyDecision notApplicable() {
        return new PolicyDecision(Effect.NOT_APPLICABLE, null, null);
    }
}
