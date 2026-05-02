package io.pragmia.virgilio.flow.nodes;

import io.pragmia.api.node.*;
import io.pragmia.virgilio.user.TotpService;
import io.pragmia.virgilio.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class TotpMfaNode implements AuthFlowNode {

    private final UserService userService;
    private final TotpService totpService;

    public String getNodeType()    { return "TOTP_MFA"; }
    public String getCategory()    { return "MFA"; }
    public String getIcon()        { return "shield-check"; }
    public String getDescription() { return "Secondo fattore TOTP RFC 6238 (Google Authenticator compatible)"; }
    public List<String> getOutputPorts() { return List.of("success", "failure"); }
    public List<NodeProperty> getProperties() {
        return List.of(new NodeProperty("window", "Finestra tolleranza", NodeProperty.PropertyType.INTEGER,
            false, 1, "Numero di step ±30s tollerati"));
    }

    public NodeExecutionResult execute(NodeContext ctx) {
        String userId = (String) ctx.getAllAttributes().get("user_id");
        String code   = (String) ctx.getAllAttributes().get("totp_code");
        if (userId == null) return NodeExecutionResult.failure("No user_id in context — place after USERNAME_PASSWORD");
        if (code == null || code.isBlank()) return NodeExecutionResult.pending();

        return userService.findById(UUID.fromString(userId))
            .filter(u -> u.isTotpEnabled() && u.getTotpSecret() != null)
            .map(u -> totpService.verify(u, code)
                ? NodeExecutionResult.success("success")
                : NodeExecutionResult.failure("Invalid TOTP code"))
            .orElse(NodeExecutionResult.failure("User not found or TOTP not configured"));
    }
}
