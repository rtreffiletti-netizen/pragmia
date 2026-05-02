package io.pragmia.virgilio.access.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "pragmia_risk_profile")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RiskProfile {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 128)
    private String name;

    @Column(nullable = false)
    private int riskThreshold = 50;  // score 0-100 > threshold = action

    @Column(nullable = false, length = 32)
    private String allowAction = "ALLOW";  // ALLOW

    @Column(nullable = false, length = 32)
    private String mfaStepupAction = "MFA_STEPUP";  // MFA_STEPUP

    @Column(nullable = false, length = 32)
    private String blockAction = "BLOCK";  // BLOCK

    @Column(nullable = false)
    private boolean isDefault = false;

    @ManyToMany(cascade = {CascadeType.MERGE})
    @JoinTable(name = "pragmia_risk_profile_factors",
        joinColumns = @JoinColumn(name = "profile_id"),
        inverseJoinColumns = @JoinColumn(name = "factor_id"))
    private Set<RiskFactor> factors = new HashSet<>();

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    @PreUpdate
    public void preUpdate() {}
}
