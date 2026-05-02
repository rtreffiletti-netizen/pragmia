package io.pragmia.virgilio.flow.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FlowDefinition {
    private List<FlowNode> nodes;
    private List<FlowEdge> edges;

    @Data @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FlowNode {
        private String id;
        private String type;
        private Map<String, Object> data;
        private Position position;
    }

    @Data @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FlowEdge {
        private String id;
        private String source;
        private String target;
        private String sourceHandle;
    }

    @Data
    public static class Position {
        private double x;
        private double y;
    }
}
