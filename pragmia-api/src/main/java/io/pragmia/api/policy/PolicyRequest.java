package io.pragmia.api.policy;

import java.util.Map;

public record PolicyRequest(
    String subjectId,
    Map<String, Object> subjectAttributes,
    String resource,
    String action,
    Map<String, Object> contextAttributes
) {}
