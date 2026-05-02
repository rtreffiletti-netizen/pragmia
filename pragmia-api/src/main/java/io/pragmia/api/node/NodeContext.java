package io.pragmia.api.node;

import java.util.Map;
import java.util.Optional;

public interface NodeContext {
    String getSessionId();
    String getClientId();
    String getRedirectUri();
    String getRemoteIp();
    String getUserAgent();
    Optional<String> getUsername();
    Optional<String> getAttribute(String key);
    Map<String, Object> getAllAttributes();
    void setAttribute(String key, Object value);
    void setUsername(String username);
    void setUserId(String userId);
}
