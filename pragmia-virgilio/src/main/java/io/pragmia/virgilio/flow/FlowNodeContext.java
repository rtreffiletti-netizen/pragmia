package io.pragmia.virgilio.flow;

import io.pragmia.api.node.NodeContext;
import lombok.Getter;
import lombok.Setter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Getter @Setter
public class FlowNodeContext implements NodeContext {

    private final String sessionId;
    private final String clientId;
    private final String redirectUri;
    private final String remoteIp;
    private final String userAgent;
    private String username;
    private String userId;
    private final Map<String, Object> attributes = new ConcurrentHashMap<>();

    public FlowNodeContext(String sessionId, String clientId,
                           String redirectUri, String remoteIp, String userAgent) {
        this.sessionId   = sessionId;
        this.clientId    = clientId;
        this.redirectUri = redirectUri;
        this.remoteIp    = remoteIp;
        this.userAgent   = userAgent;
    }

    @Override public Optional<String> getUsername()          { return Optional.ofNullable(username); }
    @Override public Optional<String> getAttribute(String k) { return Optional.ofNullable((String) attributes.get(k)); }
    @Override public Map<String, Object> getAllAttributes()  { return Collections.unmodifiableMap(attributes); }
    @Override public void setAttribute(String k, Object v)  { attributes.put(k, v); }
}
