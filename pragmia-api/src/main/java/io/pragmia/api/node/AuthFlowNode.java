package io.pragmia.api.node;

import java.util.List;

public interface AuthFlowNode {
    String getNodeType();
    String getCategory();
    String getIcon();
    String getDescription();
    List<NodeProperty> getProperties();
    List<String> getOutputPorts();
    NodeExecutionResult execute(NodeContext context);
}
