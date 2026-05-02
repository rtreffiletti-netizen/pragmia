package io.pragmia.api.node;

public record NodeExecutionResult(Status status, String outputPort, String errorMessage) {

    public enum Status { SUCCESS, FAILURE, PENDING }

    public static NodeExecutionResult success(String port) {
        return new NodeExecutionResult(Status.SUCCESS, port, null);
    }
    public static NodeExecutionResult failure(String reason) {
        return new NodeExecutionResult(Status.FAILURE, "failure", reason);
    }
    public static NodeExecutionResult pending() {
        return new NodeExecutionResult(Status.PENDING, null, null);
    }
}
