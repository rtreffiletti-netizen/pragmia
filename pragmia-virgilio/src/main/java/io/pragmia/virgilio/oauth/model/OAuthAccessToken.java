package io.pragmia.virgilio.oauth.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "oauth_access_tokens", indexes = {
    @Index(name = "idx_token_value", columnList = "tokenValue", unique = true),
    @Index(name = "idx_user_client", columnList = "userId, clientId")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OAuthAccessToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, columnDefinition = "TEXT")
    private String tokenValue; // JWT or opaque token

    @Column(nullable = false, length = 20)
    private String tokenType = "Bearer";

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

    @Column(length = 500)
    private String audience;

    @Column(length = 500)
    private String issuer;

    @Column
    private UUID refreshTokenId; // Link to refresh token

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
