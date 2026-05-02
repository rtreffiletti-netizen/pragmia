package io.pragmia.canto.model;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FlowTreeEdge {
    private String outcome;
    private String targetNodeId;
}
