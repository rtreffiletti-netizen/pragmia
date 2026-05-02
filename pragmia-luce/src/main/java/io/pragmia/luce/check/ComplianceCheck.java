package io.pragmia.luce.check;

import io.pragmia.luce.model.ControlResult;

public interface ComplianceCheck {
    String getControlId();
    ControlResult execute();
}
