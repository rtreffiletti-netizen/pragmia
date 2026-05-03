package io.pragmia.virgilio.oauth.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "oauth_authorization_codes", indexes = {
    @Index(name = "idx_code", columnList = "code", unique = true)
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OAuthAuthorizationCode {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 256)
    private String code;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false, length = 100)
    private String clientId;

    @Column(length = 500)
    private String redirectUri;

    @Column(length = 1000)
    private String scope;

    @Column(nullable = false)
    private Instant issuedAt;

    @Column(nullable = false)
    private Instant expiresAt;

    @Column(length = 128)
    private String codeChallenge; // PKCE

    @Column(length = 20)
    private String codeChallengeMethod; // plain or S256

    @Column(nullable = false)
    private boolean used = false;

    @Column(length = 50)
    private String state; // OAuth2 state parameter

    @Column(length = 100)
    private String nonce; // OIDC nonce

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    public boolean isValid() {
        return !used && !isExpired();
    }
}
