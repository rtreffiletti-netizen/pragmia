package io.pragmia.saml.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.Instant;

@Data
@Entity
@Table(name = "pragmia_saml_service_providers")
public class SamlServiceProvider {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, unique = true)
    private String entityId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String acsUrl;

    private String sloUrl;
    private String metadataUrl;

    /** Certificato X.509 PEM per verifica firma richieste */
    @Column(columnDefinition = "TEXT")
    private String signingCertificate;

    /** Attributi da includere nell'assertion (CSV) */
    private String attributeMapping;

    /** Flusso consentito: sp-initiated | idp-initiated | both */
    @Column(nullable = false)
    private String allowedFlow = "both";

    private boolean enabled = true;
    private boolean requireSignedRequests = true;
    private boolean encryptAssertions = false;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    private Instant updatedAt;

    @PreUpdate
    void onUpdate() { this.updatedAt = Instant.now(); }
}
