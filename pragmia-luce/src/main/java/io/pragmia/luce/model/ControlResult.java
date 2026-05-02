package io.pragmia.luce.model;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ControlResult {
    private String controlId;
    private String title;
    private ControlStatus status;
    private String details;
    private String remediation;
}
