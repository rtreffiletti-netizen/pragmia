package io.pragmia.virgilio.flow;

public record FlowExecutionResult(Status status, String userId, String username,
                                   String pendingNodeType, String denyReason) {
    public enum Status { ALLOWED, DENIED, PENDING }

    public static FlowExecutionResult allowed(String uid, String u) {
        return new FlowExecutionResult(Status.ALLOWED, uid, u, null, null);
    }
    public static FlowExecutionResult denied(String reason) {
        return new FlowExecutionResult(Status.DENIED, null, null, null, reason);
    }
    public static FlowExecutionResult pending(String nodeType) {
        return new FlowExecutionResult(Status.PENDING, null, null, nodeType, null);
    }

    public boolean isAllowed() { return status == Status.ALLOWED; }
    public boolean isDenied()  { return status == Status.DENIED;  }
    public boolean isPending() { return status == Status.PENDING; }
}
