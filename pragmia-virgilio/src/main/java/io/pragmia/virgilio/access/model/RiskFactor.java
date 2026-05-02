package io.pragmia.virgilio.access.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "pragmia_risk_factor")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RiskFactor {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 64)
    private String name;  // 'GEO', 'IP_REPUTATION', 'DEVICE_FINGERPRINT', 'VELOCITY', 'TIME_OF_DAY'

    @Column(nullable = false)
    private int weight = 10;  // 0-100 peso nel calcolo rischio totale

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "JSONB")
    private String config;  // es. {"allowed_countries":["IT","FR"],"blocked_countries":["XX"]}

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    @PreUpdate
    public void preUpdate() {}

    public enum FactorName {
        GEO("Geolocation country risk"),
        IP_REPUTATION("IP reputation: proxy/tor/darknet"),
        DEVICE_FINGERPRINT("Known device vs unknown device"),
        VELOCITY("Login frequency anomaly"),
        TIME_OF_DAY("Unusual login time of day");

        private final String description;
        FactorName(String desc) { this.description = desc; }
        public String getDescription() { return description; }
    }
}
