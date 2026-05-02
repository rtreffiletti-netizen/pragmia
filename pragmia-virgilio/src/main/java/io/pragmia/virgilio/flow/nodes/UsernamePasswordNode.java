package io.pragmia.virgilio.flow.nodes;

import io.pragmia.api.node.*;
import io.pragmia.virgilio.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
@RequiredArgsConstructor
public class UsernamePasswordNode implements AuthFlowNode {

    private final UserService userService;

    public String getNodeType()    { return "USERNAME_PASSWORD"; }
    public String getCategory()    { return "Authentication"; }
    public String getIcon()        { return "user"; }
    public String getDescription() { return "Autenticazione con username e password (BCrypt)"; }
    public List<String> getOutputPorts() { return List.of("success", "failure"); }
    public List<NodeProperty> getProperties() {
        return List.of(new NodeProperty("maxAttempts", "Max tentativi", NodeProperty.PropertyType.INTEGER,
            false, 5, "Tentativi prima del blocco account"));
    }

    public NodeExecutionResult execute(NodeContext ctx) {
        String username = (String) ctx.getAllAttributes().get("username");
        String password = (String) ctx.getAllAttributes().get("password");
        if (username == null || password == null || username.isBlank() || password.isBlank())
            return NodeExecutionResult.failure("Missing credentials");

        return userService.findByUsername(username)
            .filter(u -> u.isEnabled() && !u.isLocked())
            .filter(u -> userService.verifyPassword(u, password))
            .map(u -> {
                ctx.setUsername(u.getUsername());
                ctx.setUserId(u.getId().toString());
                ctx.setAttribute("user_id", u.getId().toString());
                ctx.setAttribute("user_email", u.getEmail());
                return NodeExecutionResult.success("success");
            })
            .orElse(NodeExecutionResult.failure("Invalid credentials"));
    }
}
