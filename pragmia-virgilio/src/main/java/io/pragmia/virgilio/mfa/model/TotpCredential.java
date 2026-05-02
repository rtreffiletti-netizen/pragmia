package io.pragmia.virgilio.mfa.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "pragmia_totp_credential")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TotpCredential {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private VirgilioUser user;

    @Column(nullable = false)
    private String secret;

    @Column(nullable = false, length = 16)
    private String algorithm = "SHA1";  -- SHA1, SHA256, SHA512

    @Column(nullable = false)
    private int digits = 6;  -- 6 or 8

    @Column(nullable = false)
    private int window = 1;

    @Column(nullable = false)
    private int period = 30;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(name = "last_verified_at")
    private Instant lastVerifiedAt;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    @PreUpdate
    public void preUpdate() { lastVerifiedAt = Instant.now(); }
}
