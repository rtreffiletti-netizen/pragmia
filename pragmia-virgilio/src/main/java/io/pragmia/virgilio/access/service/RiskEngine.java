package io.pragmia.virgilio.access.service;

import io.pragmia.virgilio.access.model.LoginContext;
import io.pragmia.virgilio.access.model.RiskFactor;
import io.pragmia.virgilio.access.model.RiskProfile;
import io.pragmia.virgilio.access.repository.RiskFactorRepository;
import io.pragmia.virgilio.access.repository.RiskProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;

@Service
@RequiredArgsConstructor
public class RiskEngine {

    private final RiskFactorRepository riskFactorRepository;
    private final RiskProfileRepository riskProfileRepository;

    // Risk evaluation logic
    public int evaluateRisk(LoginContext context) {
        int totalRisk = 0;

        // 1. Geographic risk (location anomaly)
        totalRisk += evaluateGeographicRisk(context);

        // 2. IP reputation risk
        totalRisk += evaluateIpRisk(context);

        // 3. Device fingerprint risk
        totalRisk += evaluateDeviceRisk(context);

        // 4. Velocity risk (multiple attempts)
        totalRisk += evaluateVelocityRisk(context);

        // 5. Time-based risk (unusual login time)
        totalRisk += evaluateTimeRisk(context);

        return Math.min(totalRisk, 100);
    }

    private int evaluateGeographicRisk(LoginContext context) {
        // Check if user's location is different from usual
        // Implementation would use geolocation database
        return 0; // Placeholder
    }

    private int evaluateIpRisk(LoginContext context) {
        // Check IP reputation
        // Implementation would use IP reputation service
        return 0; // Placeholder
    }

    private int evaluateDeviceRisk(LoginContext context) {
        // Check device fingerprint
        if (context.getDeviceFingerprint() == null) {
            return 20; // Unknown device is riskier
        }
        return 0;
    }

    private int evaluateVelocityRisk(LoginContext context) {
        // Check login velocity (multiple attempts in short time)
        // Implementation would track login attempts
        return 0; // Placeholder
    }

    private int evaluateTimeRisk(LoginContext context) {
        // Check if login time is unusual
        LocalTime loginTime = context.getLoginTime().atZone(ZoneId.systemDefault()).toLocalTime();
        if (loginTime.isBefore(LocalTime.of(6, 0)) || loginTime.isAfter(LocalTime.of(23, 0))) {
            return 10; // Unusual time
        }
        return 0;
    }

    // RiskFactor CRUD operations
    public List<RiskFactor> getAllRiskFactors() {
        return riskFactorRepository.findAll();
    }

    public Optional<RiskFactor> getRiskFactorById(UUID id) {
        return riskFactorRepository.findById(id);
    }

    public RiskFactor createRiskFactor(RiskFactor riskFactor) {
        return riskFactorRepository.save(riskFactor);
    }

    public Optional<RiskFactor> updateRiskFactor(UUID id, RiskFactor riskFactor) {
        if (!riskFactorRepository.existsById(id)) {
            return Optional.empty();
        }
        riskFactor.setId(id);
        return Optional.of(riskFactorRepository.save(riskFactor));
    }

    public void deleteRiskFactor(UUID id) {
        riskFactorRepository.deleteById(id);
    }

    // RiskProfile CRUD operations
    public List<RiskProfile> getAllRiskProfiles() {
        return riskProfileRepository.findAll();
    }

    public Optional<RiskProfile> getRiskProfileById(UUID id) {
        return riskProfileRepository.findById(id);
    }

    public RiskProfile createRiskProfile(RiskProfile riskProfile) {
        return riskProfileRepository.save(riskProfile);
    }

    public Optional<RiskProfile> updateRiskProfile(UUID id, RiskProfile riskProfile) {
        if (!riskProfileRepository.existsById(id)) {
            return Optional.empty();
        }
        riskProfile.setId(id);
        return Optional.of(riskProfileRepository.save(riskProfile));
    }

    public void deleteRiskProfile(UUID id) {
        riskProfileRepository.deleteById(id);
    }
}
