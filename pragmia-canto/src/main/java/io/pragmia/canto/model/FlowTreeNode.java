package io.pragmia.canto.model;

import lombok.*;

import java.util.List;
import java.util.Map;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FlowTreeNode {
    private String id;
    private String type;
    private Map<String, Object> properties;
    private List<FlowTreeEdge> edges;
}
