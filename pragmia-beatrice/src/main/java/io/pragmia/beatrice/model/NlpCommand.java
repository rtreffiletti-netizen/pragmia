package io.pragmia.beatrice.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "beatrice_nlp_commands")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class NlpCommand {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String prompt;

    @Column(columnDefinition = "TEXT")
    private String resolvedAction;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NlpCommandStatus status;

    private String requestedBy;
    private String approvedBy;
    private String rejectedBy;

    @CreationTimestamp
    private Instant createdAt;
    private Instant decidedAt;

    @Column(length = 512)
    private String notes;
}
