package io.pragmia.api.llm;

import java.util.Map;

public record LLMTool(
    String name,
    String description,
    Map<String, Object> parameters,
    boolean requiresDualApproval,
    String riskLevel
) {}
