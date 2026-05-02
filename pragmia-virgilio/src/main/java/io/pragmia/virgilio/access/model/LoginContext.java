package io.pragmia.virgilio.access.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "pragmia_login_context")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LoginContext {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private io.pragmia.virgilio.user.model.VirgilioUser user;

    @Column(nullable = false, length = 255)
    private String username;

    @Column(nullable = false, length = 45)
    private String ipAddress;

    @Column(columnDefinition = "TEXT")
    private String userAgent;

    @Column(name = "geo_country", length = 2)
    private String geoCountry;

    @Column(name = "geo_city", length = 128)
    private String geoCity;

    @Column(name = "device_fingerprint", length = 255)
    private String deviceFingerprint;

    @Column(nullable = false)
    private Instant loginTimestamp = Instant.now();

    @Column(name = "risk_score")
    private Integer riskScore;  // 0-100

    @Column(name = "risk_factors", columnDefinition = "JSONB")
    private String riskFactors;  // es. {"geo":25,"ip":0,"device":5,"velocity":10,"time":0}

    @Column(nullable = false, length = 32)
    private String decision = "PENDING";  // PENDING, ALLOW, MFA_STEPUP, BLOCK

    @Column(name = "mfa_completed")
    private Boolean mfaCompleted = false;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    @PreUpdate
    public void preUpdate() {}
}
