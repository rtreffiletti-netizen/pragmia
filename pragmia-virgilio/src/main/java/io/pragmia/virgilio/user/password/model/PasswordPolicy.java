
package io.pragmia.virgilio.user.password.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

/**
 * Configurazione della password policy dell'istanza PRAGMIA.
 * Una sola riga attiva per istanza (id = "default").
 */
@Entity
@Table(name = "pragmia_virgilio_password_policy")
@Getter @Setter @NoArgsConstructor
public class PasswordPolicy {

    @Id
    private String id = "default";

    /** Lunghezza minima */
    @Column(nullable = false)
    private int minLength = 12;

    /** Lunghezza massima */
    @Column(nullable = false)
    private int maxLength = 128;

    /** Richiede almeno un carattere maiuscolo */
    @Column(nullable = false)
    private boolean requireUppercase = true;

    /** Richiede almeno un carattere minuscolo */
    @Column(nullable = false)
    private boolean requireLowercase = true;

    /** Richiede almeno un numero */
    @Column(nullable = false)
    private boolean requireDigit = true;

    /** Richiede almeno un carattere speciale */
    @Column(nullable = false)
    private boolean requireSpecial = true;

    /** Numero di password precedenti da non riutilizzare (0 = disabilitato) */
    @Column(nullable = false)
    private int historyCount = 5;

    /** Scadenza password in giorni (0 = mai) */
    @Column(nullable = false)
    private int expiryDays = 90;

    /** Giorni di preavviso scadenza password */
    @Column(nullable = false)
    private int expiryWarningDays = 14;

    /** Reset token TTL in minuti */
    @Column(nullable = false)
    private int resetTokenTtlMinutes = 30;

    @Column(nullable = false)
    private Instant updatedAt = Instant.now();

    @PreUpdate
    void onUpdate() { updatedAt = Instant.now(); }
}
