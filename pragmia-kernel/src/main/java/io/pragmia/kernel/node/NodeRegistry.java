package io.pragmia.kernel.node;

import io.pragmia.api.node.AuthFlowNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class NodeRegistry {

    private final Map<String, AuthFlowNode> nodes = new ConcurrentHashMap<>();

    public void register(AuthFlowNode node) {
        nodes.put(node.getNodeType(), node);
        log.info("[NodeRegistry] registered: {} ({})", node.getNodeType(), node.getCategory());
    }

    public Optional<AuthFlowNode> getNode(String type) {
        return Optional.ofNullable(nodes.get(type));
    }

    public List<AuthFlowNode> getAllNodes() {
        return List.copyOf(nodes.values());
    }

    public List<String> getCategories() {
        return nodes.values().stream().map(AuthFlowNode::getCategory).distinct().sorted().toList();
    }

    public int getNodeCount() {
        return nodes.size();
    }
}
