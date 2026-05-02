package io.pragmia.luce.check;

import io.pragmia.luce.model.ControlResult;
import io.pragmia.luce.model.ControlStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component("MFA_ENABLED_CHECK")
@RequiredArgsConstructor
public class MfaEnabledCheck implements ComplianceCheck {

    private final JdbcTemplate jdbc;

    @Override
    public String getControlId() { return "NIS2-ART21-MFA"; }

    @Override
    public ControlResult execute() {
        try {
            int total = jdbc.queryForObject(
                "SELECT COUNT(*) FROM virgilio_users WHERE enabled = true", Integer.class);
            int withMfa = jdbc.queryForObject(
                "SELECT COUNT(*) FROM virgilio_users WHERE enabled = true AND totp_secret IS NOT NULL", Integer.class);

            if (total == 0) {
                return ControlResult.builder()
                    .controlId(getControlId())
                    .title("MFA abilitato per tutti gli utenti attivi")
                    .status(ControlStatus.NOT_APPLICABLE)
                    .details("Nessun utente attivo trovato")
                    .build();
            }

            double pct = (double) withMfa / total * 100;
            boolean pass = pct >= 100.0;
            return ControlResult.builder()
                .controlId(getControlId())
                .title("MFA abilitato per tutti gli utenti attivi")
                .status(pass ? ControlStatus.PASS : ControlStatus.FAIL)
                .details(String.format("%d/%d utenti con MFA attivo (%.1f%%)", withMfa, total, pct))
                .remediation(pass ? null : "Abilitare TOTP per tutti gli utenti privi di MFA")
                .build();
        } catch (Exception e) {
            return ControlResult.builder()
                .controlId(getControlId()).title("MFA check")
                .status(ControlStatus.FAIL).details("Errore: " + e.getMessage()).build();
        }
    }
}
