package io.pragmia.saml.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "pragmia_saml_sp")
@Getter @Setter @NoArgsConstructor
public class SamlServiceProvider {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false) private String name;
    @Column(nullable = false, unique = true) private String entityId;
    @Column private String acsUrl;
    @Column private String logoutUrl;
    @Column(columnDefinition = "TEXT") private String metadata;
    @Column private String attributeMapping;
    @Column(nullable = false) private boolean jitProvisioning = true;
    @Column(nullable = false) private boolean active = true;
    @Column(nullable = false) private Instant createdAt = Instant.now();
    @Column(nullable = false) private Instant updatedAt = Instant.now();
    @PreUpdate public void preUpdate() { updatedAt = Instant.now(); }
}
