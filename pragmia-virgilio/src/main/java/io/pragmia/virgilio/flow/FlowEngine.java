package io.pragmia.virgilio.flow;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.pragmia.api.node.*;
import io.pragmia.kernel.node.NodeRegistry;
import io.pragmia.virgilio.flow.model.FlowDefinition;
import io.pragmia.virgilio.flow.model.FlowDefinition.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FlowEngine {

    private static final int MAX_STEPS = 50;

    private final NodeRegistry nodeRegistry;
    private final ObjectMapper mapper;

    public FlowExecutionResult execute(String flowJson, FlowNodeContext ctx) {
        FlowDefinition flow;
        try { flow = mapper.readValue(flowJson, FlowDefinition.class); }
        catch (Exception e) { return FlowExecutionResult.denied("Invalid flow JSON: " + e.getMessage()); }

        Map<String, FlowNode> nodeMap = flow.getNodes().stream()
            .collect(Collectors.toMap(FlowNode::getId, n -> n));

        // edgeMap: "sourceId:handle" → targetId
        Map<String, String> edgeMap = new HashMap<>();
        for (FlowEdge e : flow.getEdges()) {
            String handle = e.getSourceHandle() != null ? e.getSourceHandle() : "";
            edgeMap.put(e.getSource() + ":" + handle, e.getTarget());
        }

        FlowNode current = flow.getNodes().stream()
            .filter(n -> "START".equals(n.getType()))
            .findFirst().orElse(null);
        if (current == null) return FlowExecutionResult.denied("No START node in flow");

        for (int step = 0; step < MAX_STEPS; step++) {
            String type = current.getType();

            if ("ALLOW".equals(type)) return FlowExecutionResult.allowed(ctx.getUserId(), ctx.getUsername().orElse(null));
            if ("DENY".equals(type))  return FlowExecutionResult.denied("Access denied by flow policy");

            if ("START".equals(type)) {
                String nextId = edgeMap.get(current.getId() + ":");
                current = nodeMap.get(nextId);
                if (current == null) return FlowExecutionResult.denied("Dead-end after START");
                continue;
            }

            Optional<AuthFlowNode> impl = nodeRegistry.getNode(type);
            if (impl.isEmpty()) return FlowExecutionResult.denied("Unknown node type: " + type);

            if (current.getData() != null) current.getData().forEach(ctx::setAttribute);

            NodeExecutionResult result = impl.get().execute(ctx);

            if (result.status() == NodeExecutionResult.Status.PENDING)
                return FlowExecutionResult.pending(type);

            String key    = current.getId() + ":" + (result.outputPort() != null ? result.outputPort() : "");
            String nextId = edgeMap.getOrDefault(key, edgeMap.get(current.getId() + ":"));
            if (nextId == null) return FlowExecutionResult.denied("No edge for port '" + result.outputPort() + "' on node " + type);

            current = nodeMap.get(nextId);
            if (current == null) return FlowExecutionResult.denied("Target node not found");
        }
        return FlowExecutionResult.denied("Max execution steps exceeded");
    }
}
