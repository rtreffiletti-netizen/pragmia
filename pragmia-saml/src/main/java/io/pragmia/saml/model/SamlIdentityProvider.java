package io.pragmia.saml.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "pragmia_saml_idp")
@Getter @Setter @NoArgsConstructor
public class SamlIdentityProvider {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false) private String name;
    @Column(nullable = false, unique = true) private String entityId;
    @Column(nullable = false) private String ssoUrl;
    @Column private String sloUrl;
    @Column(columnDefinition = "TEXT") private String certificate;
    @Column(columnDefinition = "TEXT") private String privateKey;
    @Column private String attributeMapping;
    @Column(nullable = false) private boolean active = true;
    @Column(nullable = false) private Instant createdAt = Instant.now();
    @Column(nullable = false) private Instant updatedAt = Instant.now();
    @PreUpdate public void preUpdate() { updatedAt = Instant.now(); }
}
