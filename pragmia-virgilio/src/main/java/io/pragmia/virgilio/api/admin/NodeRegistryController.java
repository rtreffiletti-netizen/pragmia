package io.pragmia.virgilio.api.admin;

import io.pragmia.kernel.node.NodeRegistry;
import io.pragmia.api.node.AuthFlowNode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/admin/v1/nodes")
@RequiredArgsConstructor
@Tag(name = "Admin - Nodes", description = "Catalogo nodi disponibili per CANTO Flow Editor")
public class NodeRegistryController {

    private final NodeRegistry nodeRegistry;

    @GetMapping @Operation(summary = "Tutti i tipi di nodo disponibili")
    public ResponseEntity<List<Map<String, Object>>> list() {
        return ResponseEntity.ok(nodeRegistry.getAllNodes().stream()
            .map(n -> Map.of(
                "nodeType",    (Object) n.getNodeType(),
                "category",    n.getCategory(),
                "icon",        n.getIcon(),
                "description", n.getDescription(),
                "outputPorts", n.getOutputPorts()))
            .sorted(Comparator.comparing(m -> m.get("category").toString()))
            .toList());
    }

    @GetMapping("/categories") @Operation(summary = "Categorie nodi")
    public ResponseEntity<List<String>> categories() {
        return ResponseEntity.ok(nodeRegistry.getCategories());
    }
}
