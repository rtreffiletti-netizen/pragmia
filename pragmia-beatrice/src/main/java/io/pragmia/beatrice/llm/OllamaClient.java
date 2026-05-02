package io.pragmia.beatrice.llm;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.pragmia.api.llm.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class OllamaClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper mapper;

    @Value("${pragmia.beatrice.ollama.base-url:http://localhost:11434}")
    private String baseUrl;

    @Value("${pragmia.beatrice.ollama.model:llama3.2}")
    private String model;

    private static final String SYSTEM_PROMPT = """
        Sei BEATRICE, l'assistente NLP di PRAGMIA (IAM platform).
        Hai accesso agli strumenti di amministrazione utenti, sessioni e policy.
        Rispondi SEMPRE in JSON strutturato con il tool_call da eseguire o un messaggio.
        Operazioni distruttive richiedono doppia approvazione (dual-approval).
        Non inventare dati. Se non capisci, chiedi chiarimenti.
        """;

    public String chat(String userPrompt, List<LLMTool> tools) {
        try {
            var messages = List.of(
                Map.of("role", "system", "content", SYSTEM_PROMPT),
                Map.of("role", "user",   "content", userPrompt)
            );
            var body = Map.of(
                "model",    model,
                "messages", messages,
                "stream",   false
            );
            var response = restTemplate.postForObject(baseUrl + "/api/chat", body, Map.class);
            if (response != null && response.get("message") instanceof Map msg) {
                return (String) msg.get("content");
            }
            return "{}";
        } catch (Exception e) {
            log.error("[BEATRICE] Ollama error: {}", e.getMessage());
            return "{\"error\": \"" + e.getMessage() + "\"}";
        }
    }
}
