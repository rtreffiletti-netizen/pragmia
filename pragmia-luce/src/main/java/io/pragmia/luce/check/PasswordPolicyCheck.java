package io.pragmia.luce.check;

import io.pragmia.luce.model.ControlResult;
import io.pragmia.luce.model.ControlStatus;
import org.springframework.stereotype.Component;

@Component("PASSWORD_POLICY_CHECK")
public class PasswordPolicyCheck implements ComplianceCheck {

    @Override
    public String getControlId() { return "AGID-PWD-POLICY"; }

    @Override
    public ControlResult execute() {
        // In produzione: verifica la configurazione effettiva del password encoder
        return ControlResult.builder()
            .controlId(getControlId())
            .title("Password policy AgID conforme (min 8 char, complessità)")
            .status(ControlStatus.MANUAL_CHECK_REQUIRED)
            .details("Verifica manuale della configurazione password encoder richiesta")
            .remediation("Configurare PasswordEncoder con BCrypt strength >= 12 e validazione pattern")
            .build();
    }
}
