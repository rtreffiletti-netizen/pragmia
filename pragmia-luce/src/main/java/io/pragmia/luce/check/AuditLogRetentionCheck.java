package io.pragmia.luce.check;

import io.pragmia.luce.model.ControlResult;
import io.pragmia.luce.model.ControlStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Component("AUDIT_RETENTION_CHECK")
@RequiredArgsConstructor
public class AuditLogRetentionCheck implements ComplianceCheck {

    private final JdbcTemplate jdbc;

    @Value("${pragmia.luce.audit-retention-days:365}")
    private int requiredDays;

    @Override
    public String getControlId() { return "NIS2-ART21-AUDIT"; }

    @Override
    public ControlResult execute() {
        try {
            Instant threshold = Instant.now().minus(requiredDays, ChronoUnit.DAYS);
            Integer oldCount = jdbc.queryForObject(
                "SELECT COUNT(*) FROM clio_audit_log WHERE ts < ?",
                Integer.class, threshold);
            boolean pass = oldCount != null && oldCount == 0;
            return ControlResult.builder()
                .controlId(getControlId())
                .title("Audit log retention >= " + requiredDays + " giorni")
                .status(pass ? ControlStatus.PASS : ControlStatus.MANUAL_CHECK_REQUIRED)
                .details(pass
                    ? "Nessun evento più vecchio di " + requiredDays + " giorni trovato"
                    : oldCount + " eventi potrebbero essere soggetti a policy di retention")
                .remediation(pass ? null : "Verificare la policy di archiviazione del log su storage esterno")
                .build();
        } catch (Exception e) {
            return ControlResult.builder()
                .controlId(getControlId()).title("Audit retention check")
                .status(ControlStatus.FAIL).details("Errore: " + e.getMessage()).build();
        }
    }
}
