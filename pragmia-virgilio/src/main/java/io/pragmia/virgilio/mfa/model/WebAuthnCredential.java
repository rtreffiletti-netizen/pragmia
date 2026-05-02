package io.pragmia.virgilio.mfa.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "pragmia_webauthn_credential")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class WebAuthnCredential {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private VirgilioUser user;

    @Column(nullable = false)
    private byte[] credentialId;

    @Column(nullable = false)
    private byte[] publicKey;

    @Column(nullable = false)
    private long counter = 0;

    @Column(name = "attestation_object")
    private byte[] attestationObject;

    @Column(name = "user_label")
    private String userLabel;

    @Column(name = "transport")
    private String transport;

    @Column(name = "aa_guid")
    private UUID aaguid;

    @Column(name = "authenticator_type", length = 32)
    private String authenticatorType = "platform";

    @Column(name = "verify_instant")
    private Instant verifyInstant;

    @Column(name = "backup_eligible")
    private boolean backupEligible = false;

    @Column(name = "backup_state")
    private boolean backupState = false;

    @Column(name = "userless", nullable = false)
    private boolean userless = false;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "last_used_at")
    private Instant lastUsedAt;

    @PreUpdate
    public void preUpdate() { verifyInstant = Instant.now(); }
}
