package io.pragmia.canto.dag;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.pragmia.canto.model.FlowTreeEdge;
import io.pragmia.canto.model.FlowTreeNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@RequiredArgsConstructor
public class DagValidator {

    private final ObjectMapper objectMapper;

    public DagValidationResult validate(String definitionJson) {
        try {
            var root = objectMapper.readTree(definitionJson);
            var nodesNode = root.get("nodes");
            if (nodesNode == null || !nodesNode.isArray()) {
                return DagValidationResult.fail("Missing or invalid 'nodes' array");
            }

            List<FlowTreeNode> nodes = new ArrayList<>();
            for (var n : nodesNode) {
                var node = objectMapper.treeToValue(n, FlowTreeNode.class);
                nodes.add(node);
            }

            if (nodes.isEmpty()) return DagValidationResult.fail("Flow must have at least one node");

            // Check for duplicate IDs
            Set<String> ids = new HashSet<>();
            for (var node : nodes) {
                if (!ids.add(node.getId())) {
                    return DagValidationResult.fail("Duplicate node id: " + node.getId());
                }
            }

            // Check all edge targets exist
            for (var node : nodes) {
                if (node.getEdges() != null) {
                    for (FlowTreeEdge edge : node.getEdges()) {
                        if (!ids.contains(edge.getTargetNodeId())) {
                            return DagValidationResult.fail(
                                "Node '" + node.getId() + "' references unknown target: " + edge.getTargetNodeId());
                        }
                    }
                }
            }

            // Cycle detection (DFS)
            Set<String> visited = new HashSet<>();
            Set<String> stack = new HashSet<>();
            Map<String, List<String>> adj = buildAdjacency(nodes);
            for (var node : nodes) {
                if (hasCycle(node.getId(), adj, visited, stack)) {
                    return DagValidationResult.fail("Cycle detected in flow graph");
                }
            }

            return DagValidationResult.ok(nodes.size());
        } catch (Exception e) {
            return DagValidationResult.fail("Invalid JSON: " + e.getMessage());
        }
    }

    private Map<String, List<String>> buildAdjacency(List<FlowTreeNode> nodes) {
        Map<String, List<String>> adj = new HashMap<>();
        for (var n : nodes) {
            List<String> targets = new ArrayList<>();
            if (n.getEdges() != null) {
                for (var e : n.getEdges()) targets.add(e.getTargetNodeId());
            }
            adj.put(n.getId(), targets);
        }
        return adj;
    }

    private boolean hasCycle(String node, Map<String, List<String>> adj,
                              Set<String> visited, Set<String> stack) {
        if (stack.contains(node)) return true;
        if (visited.contains(node)) return false;
        visited.add(node);
        stack.add(node);
        for (String neighbor : adj.getOrDefault(node, List.of())) {
            if (hasCycle(neighbor, adj, visited, stack)) return true;
        }
        stack.remove(node);
        return false;
    }
}
