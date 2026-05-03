package io.pragmia.saml.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.Instant;

@Data
@Entity
@Table(name = "pragmia_saml_audit")
public class SamlAuditEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private Instant timestamp = Instant.now();

    /** SSO_REQUEST | SSO_SUCCESS | SSO_FAILURE | SLO_REQUEST | SLO_SUCCESS | METADATA_REQUEST */
    @Column(nullable = false)
    private String eventType;

    private String userId;
    private String spEntityId;
    private String idpEntityId;
    private String sessionIndex;
    private String clientIp;
    private String result;

    @Column(columnDefinition = "TEXT")
    private String details;
}
