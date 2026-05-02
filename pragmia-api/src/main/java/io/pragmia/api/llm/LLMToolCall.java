package io.pragmia.api.llm;

import java.util.Map;

public record LLMToolCall(String id, String toolName, Map<String, Object> arguments) {}
