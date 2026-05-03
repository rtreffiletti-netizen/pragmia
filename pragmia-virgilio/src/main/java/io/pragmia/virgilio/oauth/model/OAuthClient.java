package io.pragmia.virgilio.oauth.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "oauth_clients", indexes = {
    @Index(name = "idx_client_id", columnList = "clientId", unique = true)
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OAuthClient {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 100)
    private String clientId;

    @Column(nullable = false, length = 256)
    private String clientSecret; // Hashed

    @Column(nullable = false, length = 200)
    private String clientName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "oauth_client_redirect_uris", 
                     joinColumns = @JoinColumn(name = "client_id"))
    @Column(name = "redirect_uri", length = 500)
    private Set<String> redirectUris;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "oauth_client_grant_types",
                     joinColumns = @JoinColumn(name = "client_id"))
    @Column(name = "grant_type", length = 50)
    private Set<String> authorizedGrantTypes; // authorization_code, client_credentials, refresh_token, etc.

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "oauth_client_scopes",
                     joinColumns = @JoinColumn(name = "client_id"))
    @Column(name = "scope", length = 100)
    private Set<String> scopes;

    @Column(nullable = false)
    private Integer accessTokenValidity = 3600; // seconds (1 hour)

    @Column(nullable = false)
    private Integer refreshTokenValidity = 2592000; // seconds (30 days)

    @Column(nullable = false)
    private boolean autoApprove = false;

    @Column(nullable = false)
    private boolean pkceRequired = false; // PKCE for public clients

    @Column(length = 1000)
    private String jwksUri; // JSON Web Key Set URI for JWT validation

    @Column(length = 500)
    private String tokenEndpointAuthMethod = "client_secret_basic"; // or client_secret_post, private_key_jwt

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "oauth_client_post_logout_uris",
                     joinColumns = @JoinColumn(name = "client_id"))
    @Column(name = "post_logout_uri", length = 500)
    private Set<String> postLogoutRedirectUris;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(nullable = false)
    private Instant createdAt;

    @Column
    private Instant updatedAt;

    @Column(length = 100)
    private String createdBy;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
