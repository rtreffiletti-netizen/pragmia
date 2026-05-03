package io.pragmia.virgilio.session.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_sessions", indexes = {
    @Index(name = "idx_session_token", columnList = "sessionToken", unique = true),
    @Index(name = "idx_user_id", columnList = "userId"),
    @Index(name = "idx_expiry", columnList = "expiresAt")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 256)
    private String sessionToken;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private String ipAddress;

    @Column(nullable = false, length = 512)
    private String userAgent;

    @Column(length = 256)
    private String deviceFingerprint;

    @Column(length = 100)
    private String geolocation; // Country code or city

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant lastAccessedAt;

    @Column(nullable = false)
    private Instant expiresAt;

    @Column(nullable = false)
    private boolean active = true;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SessionStatus status = SessionStatus.ACTIVE;

    @Column(columnDefinition = "TEXT")
    private String metadata; // Additional session metadata in JSON format

    @PreUpdate
    public void preUpdate() {
        this.lastAccessedAt = Instant.now();
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    public boolean isValid() {
        return active && status == SessionStatus.ACTIVE && !isExpired();
    }
}
