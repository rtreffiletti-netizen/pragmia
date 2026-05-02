package io.pragmia.virgilio.flow.nodes;

import io.pragmia.api.node.*;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class DenyNode implements AuthFlowNode {
    public String getNodeType()    { return "DENY"; }
    public String getCategory()    { return "Terminal"; }
    public String getIcon()        { return "x-circle"; }
    public String getDescription() { return "Accesso negato — fine del flusso con DENY"; }
    public List<String> getOutputPorts() { return List.of(); }
    public List<NodeProperty> getProperties() {
        return List.of(new NodeProperty("reason", "Messaggio", NodeProperty.PropertyType.STRING,
            false, "Access denied", "Messaggio mostrato all'utente"));
    }
    public NodeExecutionResult execute(NodeContext ctx) {
        String reason = (String) ctx.getAllAttributes().getOrDefault("reason", "Access denied");
        return NodeExecutionResult.failure(reason);
    }
}
