package io.pragmia.luce.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "luce_compliance_controls")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ComplianceControl {

    @Id
    @Column(nullable = false)
    private String id;               // es. NIS2-ART21-MFA

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ComplianceFramework framework;

    @Column(nullable = false)
    private String articleRef;       // es. Art.21(2)(a)

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private String checkBean;        // Spring bean name da invocare per il check

    @Column(nullable = false)
    private boolean automated = true;
}
