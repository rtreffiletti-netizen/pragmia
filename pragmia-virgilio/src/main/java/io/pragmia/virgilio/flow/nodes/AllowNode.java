package io.pragmia.virgilio.flow.nodes;

import io.pragmia.api.node.*;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class AllowNode implements AuthFlowNode {
    public String getNodeType()    { return "ALLOW"; }
    public String getCategory()    { return "Terminal"; }
    public String getIcon()        { return "check-circle"; }
    public String getDescription() { return "Accesso concesso — fine del flusso con PERMIT"; }
    public List<String>       getOutputPorts() { return List.of(); }
    public List<NodeProperty> getProperties()  { return List.of(); }
    public NodeExecutionResult execute(NodeContext ctx) { return NodeExecutionResult.success("allow"); }
}
