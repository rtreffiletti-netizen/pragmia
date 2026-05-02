package io.pragmia.luce.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.List;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "luce_compliance_reports")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ComplianceReport {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ComplianceFramework framework;

    @CreationTimestamp
    private Instant generatedAt;

    private String generatedBy;

    @Column(nullable = false)
    private int totalControls;

    @Column(nullable = false)
    private int passedControls;

    @Column(nullable = false)
    private int failedControls;

    @Column(nullable = false)
    private int notApplicable;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<ControlResult> results;

    public double getComplianceScore() {
        if (totalControls == 0) return 0;
        return (double) passedControls / totalControls * 100;
    }
}
