package io.pragmia.api.llm;

import java.util.List;

public interface LLMProvider {
    String getName();
    boolean isAvailable();
    LLMResponse complete(LLMRequest request);
    List<LLMTool> getAvailableTools();
}
