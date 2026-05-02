package io.pragmia.clio.model;

import io.pragmia.api.audit.AuditEventType;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.Map;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "clio_audit_log",
    indexes = {
        @Index(name = "idx_clio_user_id",    columnList = "user_id"),
        @Index(name = "idx_clio_event_type", columnList = "event_type"),
        @Index(name = "idx_clio_timestamp",  columnList = "ts"),
        @Index(name = "idx_clio_session_id", columnList = "session_id")
    })
@Getter @NoArgsConstructor @AllArgsConstructor @Builder
public class AuditRecord {

    @Id
    @Column(name = "event_id", nullable = false, updatable = false)
    private String eventId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, updatable = false)
    private AuditEventType eventType;

    @Column(name = "ts", nullable = false, updatable = false)
    private Instant ts;

    @Column(name = "user_id",    updatable = false) private String userId;
    @Column(name = "admin_id",   updatable = false) private String adminId;
    @Column(name = "session_id", updatable = false) private String sessionId;
    @Column(name = "client_id",  updatable = false) private String clientId;
    @Column(name = "remote_ip",  updatable = false) private String remoteIp;
    @Column(name = "resource",   updatable = false) private String resource;
    @Column(name = "action",     updatable = false) private String action;
    @Column(name = "result",     updatable = false) private String result;
    @Column(name = "failure_reason", updatable = false) private String failureReason;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", updatable = false, columnDefinition = "jsonb")
    private Map<String, Object> metadata;
}
