package io.pragmia.minos.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(name = "minos_policies",
    indexes = {
        @Index(name = "idx_minos_policy_active",   columnList = "active"),
        @Index(name = "idx_minos_policy_priority", columnList = "priority")
    })
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Policy {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(length = 512)
    private String description;

    /** SpEL expression — es: subject.role == 'ADMIN' or environment['remoteIp'].startsWith('10.') */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String condition;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PolicyEffect effect;   // PERMIT | DENY

    @Column(nullable = false)
    private int priority = 100;    // ordine di valutazione (più basso = prima)

    @Column(nullable = false)
    private boolean active = true;

    @CreationTimestamp private Instant createdAt;
    @UpdateTimestamp  private Instant updatedAt;
    private String createdBy;
}
