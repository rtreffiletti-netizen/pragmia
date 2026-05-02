package io.pragmia.canto.dag;

public record DagValidationResult(boolean valid, String error, int nodeCount) {
    public static DagValidationResult ok(int n)      { return new DagValidationResult(true, null, n); }
    public static DagValidationResult fail(String e) { return new DagValidationResult(false, e, 0); }
}
