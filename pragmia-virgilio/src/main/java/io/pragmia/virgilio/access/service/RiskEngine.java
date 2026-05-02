package io.pragmia.virgilio.access.service;

import io.pragmia.virgilio.user.model.VirgilioUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class RiskEngine {

    private final io.pragmia.virgilio.access.repository.RiskFactorRepository factorRepo;
    private final io.pragmia.virgilio.access.repository.RiskProfileRepository profileRepo;
    private final io.pragmia.virgilio.access.repository.LoginContextRepository contextRepo;

    public RiskAssessment assess(VirgilioUser user, String ipAddress, String userAgent,
                                  String geoCountry, String deviceFingerprint, Instant loginTime) {
        Map<String, Integer> scores = new HashMap<>();
        int totalScore = 0;
        List<RiskFactor> factors = factorRepo.findAll();

        for (RiskFactor factor : factors) {
            if (!factor.isEnabled()) continue;
            int score = evaluateFactor(factor, user, ipAddress, userAgent, geoCountry, deviceFingerprint, loginTime);
            scores.put(factor.getName(), score);
            totalScore += score * factor.getWeight();
        }

        int normalizedScore = Math.min(100, totalScore / Math.max(1, factors.size()));
        RiskProfile profile = profileRepo.findFirstByIsDefaultTrue()
            .orElseGet(() -> { RiskProfile p = new RiskProfile(); p.setName("Default"); p.setRiskThreshold(50); return p; });

        String decision = normalizedScore <= profile.getRiskThreshold() ? "ALLOW" : "BLOCK";
        boolean requiresMfa = normalizedScore > 20 && normalizedScore <= profile.getRiskThreshold();
        if (requiresMfa) decision = "MFA_STEPUP";

        log.info("RiskEngine: user={} ip={} score={}/100 decision={}", user.getUsername(), ipAddress, normalizedScore, decision);
        return new RiskAssessment(normalizedScore, scores, decision, false);
    }

    private int evaluateFactor(RiskFactor factor, VirgilioUser user, String ipAddress,
                                String userAgent, String geoCountry, String deviceFingerprint,
                                Instant loginTime) {
        return switch (factor.getName()) {
            case "GEO" -> evaluateGeo(factor, geoCountry);
            case "IP_REPUTATION" -> evaluateIpFactor(factor, ipAddress);
            case "DEVICE_FINGERPRINT" -> evaluateDevice(factor, deviceFingerprint);
            case "VELOCITY" -> evaluateVelocity(factor, user.getUsername());
            case "TIME_OF_DAY" -> evaluateTime(factor, loginTime);
            default -> 0;
        };
    }

    private int evaluateGeo(RiskFactor factor, String geoCountry) {
        if (geoCountry == null) return factor.getWeight();
        return "IT".equals(geoCountry.toUpperCase()) ? 0 : factor.getWeight() / 2;
    }

    private int evaluateIpFactor(RiskFactor factor, String ipAddress) {
        if (ipAddress == null) return factor.getWeight();
        return ipAddress.startsWith("10.") || ipAddress.startsWith("192.168.") ? 0 : factor.getWeight() / 4;
    }

    private int evaluateDevice(RiskFactor factor, String deviceFingerprint) {
        return deviceFingerprint != null && !deviceFingerprint.isBlank() ? 0 : factor.getWeight();
    }

    private int evaluateVelocity(RiskFactor factor, String username) {
        long attemptsLast5min = contextRepo.countByUsernameAndLoginTimestampAfter(username, Instant.now().minusSeconds(300));
        return attemptsLast5min > 5 ? factor.getWeight() : (int) (factor.getWeight() * attemptsLast5min / 10.0);
    }

    private int evaluateTime(RiskFactor factor, Instant loginTime) {
        LocalTime local = LocalTime.now(ZoneId.systemDefault());
        int hour = local.getHour();
        return (hour < 6 || hour > 23) ? factor.getWeight() / 2 : 0;
    }

    public record RiskAssessment(int score, Map<String, Integer> factors, String decision, boolean mfaRequired) {}
}

interface RiskFactorRepository {
    List<RiskFactor> findAll();
    Optional<RiskFactor> findById(UUID id);
    void save(RiskFactor factor);
}

interface RiskProfileRepository {
    Optional<RiskProfile> findFirstByIsDefaultTrue();
    Optional<RiskProfile> findById(UUID id);
    void save(RiskProfile profile);
}

interface LoginContextRepository {
    long countByUsernameAndLoginTimestampAfter(String username, Instant after);
    void save(io.pragmia.virgilio.access.model.LoginContext context);
}
