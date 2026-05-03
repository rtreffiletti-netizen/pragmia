
package io.pragmia.virgilio.user.password.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

/**
 * Storico delle password precedenti di un utente.
 * Usato per impedire il riutilizzo delle ultime N password.
 */
@Entity
@Table(name = "pragmia_virgilio_password_history",
       indexes = {
           @Index(name = "idx_pwh_user_id", columnList = "userId"),
           @Index(name = "idx_pwh_created", columnList = "createdAt")
       })
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PasswordHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String passwordHash;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();
}
