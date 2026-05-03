package io.pragmia.saml.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.Instant;

@Data
@Entity
@Table(name = "pragmia_saml_sessions")
public class SamlSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private String spEntityId;

    /** NameID inviato all'SP */
    private String nameId;
    private String nameIdFormat = "urn:oasis:names:tc:SAML:1.1:nameid-format:emailAddress";

    /** SessionIndex nell'assertion */
    private String sessionIndex;

    private Instant issuedAt = Instant.now();
    private Instant expiresAt;
    private boolean active = true;

    /** Tipo di autenticazione: spid | cie | local | ldap */
    private String authType = "local";

    /** IP del client al momento dell'autenticazione */
    private String clientIp;
}
