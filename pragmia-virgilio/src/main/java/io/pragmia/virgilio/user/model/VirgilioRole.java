package io.pragmia.virgilio.user.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "pragmia_virgilio_roles")
@Getter @Setter @NoArgsConstructor
public class VirgilioRole {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false, unique = true) private String name;
    private String description;
    @Column(nullable = false) private Instant createdAt = Instant.now();
}
