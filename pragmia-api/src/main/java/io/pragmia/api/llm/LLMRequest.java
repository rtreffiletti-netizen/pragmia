package io.pragmia.api.llm;

import java.util.List;

public record LLMRequest(
    String systemPrompt,
    List<LLMMessage> messages,
    List<LLMTool> tools,
    double temperature,
    int maxTokens
) {}
