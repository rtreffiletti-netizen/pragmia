
package io.pragmia.virgilio.user.password.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

/**
 * Token monouso per il self-service password reset.
 * Invalidato immediatamente dopo il primo utilizzo.
 */
@Entity
@Table(name = "pragmia_virgilio_password_reset_tokens",
       indexes = {
           @Index(name = "idx_prt_token",   columnList = "tokenHash", unique = true),
           @Index(name = "idx_prt_user_id", columnList = "userId"),
           @Index(name = "idx_prt_expires", columnList = "expiresAt")
       })
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID userId;

    /** SHA-256 del token (il token in chiaro è inviato via email, mai persistito) */
    @Column(nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    @Column(nullable = false)
    private Instant expiresAt;

    @Column(nullable = false)
    private boolean used = false;

    @Column
    private Instant usedAt;

    /** IP da cui è stata effettuata la richiesta di reset */
    @Column(length = 64)
    private String requestIp;

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    public boolean isValid() {
        return !used && !isExpired();
    }
}
