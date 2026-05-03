package io.pragmia.virgilio.oauth.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "oauth_refresh_tokens", indexes = {
    @Index(name = "idx_token_value_rt", columnList = "tokenValue", unique = true)
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OAuthRefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, columnDefinition = "TEXT")
    private String tokenValue;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false, length = 100)
    private String clientId;

    @Column(length = 1000)
    private String scope;

    @Column(nullable = false)
    private Instant issuedAt;

    @Column(nullable = false)
    private Instant expiresAt;

    @Column(nullable = false)
    private boolean revoked = false;

    @Column
    private Instant revokedAt;

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    public boolean isValid() {
        return !revoked && !isExpired();
    }
}
