package io.pragmia.luce.check;

import io.pragmia.luce.model.ControlResult;
import io.pragmia.luce.model.ControlStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component("SESSION_TIMEOUT_CHECK")
@RequiredArgsConstructor
public class SessionTimeoutCheck implements ComplianceCheck {

    @Value("${pragmia.virgilio.session.max-idle-minutes:30}")
    private int maxIdleMinutes;

    private static final int DORA_MAX_IDLE = 30;

    @Override
    public String getControlId() { return "DORA-ART9-SESSION"; }

    @Override
    public ControlResult execute() {
        boolean pass = maxIdleMinutes <= DORA_MAX_IDLE;
        return ControlResult.builder()
            .controlId(getControlId())
            .title("Session idle timeout <= " + DORA_MAX_IDLE + " minuti (DORA Art.9)")
            .status(pass ? ControlStatus.PASS : ControlStatus.FAIL)
            .details("Timeout configurato: " + maxIdleMinutes + " minuti")
            .remediation(pass ? null : "Ridurre pragmia.virgilio.session.max-idle-minutes a <= 30")
            .build();
    }
}
