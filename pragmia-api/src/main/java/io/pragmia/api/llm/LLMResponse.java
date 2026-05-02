package io.pragmia.api.llm;

import java.util.List;

public record LLMResponse(
    String content,
    List<LLMToolCall> toolCalls,
    String finishReason,
    int promptTokens,
    int completionTokens
) {
    public boolean hasToolCalls() {
        return toolCalls != null && !toolCalls.isEmpty();
    }
}
