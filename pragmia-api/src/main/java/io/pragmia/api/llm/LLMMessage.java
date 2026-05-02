package io.pragmia.api.llm;

public record LLMMessage(String role, String content) {
    public static LLMMessage system(String c)    { return new LLMMessage("system", c); }
    public static LLMMessage user(String c)      { return new LLMMessage("user", c); }
    public static LLMMessage assistant(String c) { return new LLMMessage("assistant", c); }
}
