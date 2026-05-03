package io.pragmia.virgilio.access.service;

import io.pragmia.virgilio.access.model.LoginContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * GeoLocation Service - Integrates with RiskEngine
 * Provides geolocation analysis and impossible travel detection
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GeoLocationService {

    // Cache for user last location (userId -> LocationInfo)
    private final Map<String, LocationInfo> userLastLocation = new ConcurrentHashMap<>();

    // High-risk countries (example list - configure via properties)
    private static final Set<String> HIGH_RISK_COUNTRIES = Set.of(
            "KP", "IR", "SY", "CU", "SD", "BY" // ISO country codes
    );

    // Known VPN/Proxy/Tor IP ranges (simplified - use real database in production)
    private static final Set<String> VPN_PROXY_INDICATORS = Set.of(
            "vpn", "proxy", "tor", "relay", "exit"
    );

    /**
     * Calculates geographic risk score (0-30)
     * Used by RiskEngine.evaluateGeographicRisk()
     */
    public int calculateGeographicRisk(LoginContext context) {
        int riskScore = 0;

        String ipAddress = context.getIpAddress();
        String userId = context.getUserId();

        // 1. Country risk (0-10)
        String country = extractCountryFromIP(ipAddress);
        riskScore += calculateCountryRisk(country);

        // 2. VPN/Proxy/Tor detection (0-15)
        if (isVpnOrProxy(ipAddress)) {
            riskScore += 15;
            log.warn("VPN/Proxy detected for user: {} from IP: {}", userId, ipAddress);
        }

        // 3. Impossible travel detection (0-20)
        if (userId != null) {
            int travelRisk = detectImpossibleTravel(userId, ipAddress);
            riskScore += travelRisk;
        }

        log.info("Geographic risk for user {}: {} (country: {})", userId, riskScore, country);
        return Math.min(riskScore, 30);
    }

    /**
     * Detects impossible travel (geo-velocity)
     * Returns risk score 0-20
     */
    public int detectImpossibleTravel(String userId, String currentIp) {
        LocationInfo lastLoc = userLastLocation.get(userId);
        
        if (lastLoc == null) {
            // First login or no history
            updateUserLocation(userId, currentIp);
            return 0;
        }

        // Get current location
        LocationInfo currentLoc = resolveLocation(currentIp);
        
        // Calculate distance
        double distanceKm = calculateDistance(
                lastLoc.latitude, lastLoc.longitude,
                currentLoc.latitude, currentLoc.longitude
        );

        // Calculate time elapsed
        Duration timeDiff = Duration.between(lastLoc.timestamp, Instant.now());
        double hoursElapsed = timeDiff.toMinutes() / 60.0;

        // Calculate required speed (km/h)
        double requiredSpeed = distanceKm / Math.max(hoursElapsed, 0.01);

        // Update location
        updateUserLocation(userId, currentIp);

        // Impossible travel threshold: >900 km/h (commercial flight speed)
        if (requiredSpeed > 900) {
            log.warn("Impossible travel detected for user {}: {} km in {} hours ({} km/h)",
                    userId, distanceKm, hoursElapsed, requiredSpeed);
            return 20; // High risk
        } else if (requiredSpeed > 500) {
            log.info("Fast travel detected for user {}: {} km/h", userId, requiredSpeed);
            return 10; // Medium risk
        }

        return 0;
    }

    /**
     * Calculates country-based risk score (0-10)
     */
    private int calculateCountryRisk(String countryCode) {
        if (countryCode == null || countryCode.isEmpty()) {
            return 5; // Unknown country = medium risk
        }

        if (HIGH_RISK_COUNTRIES.contains(countryCode.toUpperCase())) {
            return 10; // High risk country
        }

        return 0; // Normal risk
    }

    /**
     * Checks if IP is VPN/Proxy/Tor
     */
    public boolean isVpnOrProxy(String ipAddress) {
        if (ipAddress == null) {
            return false;
        }

        // TODO: Integrate with real VPN detection service (IPQualityScore, MaxMind, etc.)
        // This is a simplified check
        String reverseDns = getReverseDNS(ipAddress);
        if (reverseDns != null) {
            String lowerDns = reverseDns.toLowerCase();
            return VPN_PROXY_INDICATORS.stream().anyMatch(lowerDns::contains);
        }

        return false;
    }

    /**
     * Extracts country code from IP address
     * TODO: Integrate with GeoIP database (MaxMind GeoLite2)
     */
    public String extractCountryFromIP(String ipAddress) {
        if (ipAddress == null || ipAddress.isEmpty()) {
            return "UNKNOWN";
        }

        // Simplified logic - use real GeoIP database in production
        if (ipAddress.startsWith("192.168.") || ipAddress.startsWith("10.")) {
            return "PRIVATE";
        }

        // Mock implementation - returns IT for demonstration
        return "IT";
    }

    /**
     * Resolves IP to location coordinates
     */
    private LocationInfo resolveLocation(String ipAddress) {
        // TODO: Use real GeoIP database (MaxMind GeoLite2, IP2Location)
        // Mock implementation for demonstration
        LocationInfo loc = new LocationInfo();
        loc.ipAddress = ipAddress;
        loc.latitude = 41.9028; // Rome coordinates (mock)
        loc.longitude = 12.4964;
        loc.country = extractCountryFromIP(ipAddress);
        loc.timestamp = Instant.now();
        return loc;
    }

    /**
     * Updates user last known location
     */
    private void updateUserLocation(String userId, String ipAddress) {
        LocationInfo loc = resolveLocation(ipAddress);
        userLastLocation.put(userId, loc);
    }

    /**
     * Calculates distance between two coordinates (Haversine formula)
     * Returns distance in kilometers
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371; // Earth radius in km

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return R * c;
    }

    /**
     * Gets reverse DNS for IP address
     */
    private String getReverseDNS(String ipAddress) {
        try {
            java.net.InetAddress addr = java.net.InetAddress.getByName(ipAddress);
            return addr.getCanonicalHostName();
        } catch (Exception e) {
            log.debug("Failed to resolve reverse DNS for IP: {}", ipAddress);
            return null;
        }
    }

    /**
     * Gets location history for user
     */
    public List<Map<String, Object>> getLocationHistory(String userId) {
        LocationInfo lastLoc = userLastLocation.get(userId);
        if (lastLoc == null) {
            return Collections.emptyList();
        }

        Map<String, Object> location = new HashMap<>();
        location.put("ipAddress", lastLoc.ipAddress);
        location.put("country", lastLoc.country);
        location.put("latitude", lastLoc.latitude);
        location.put("longitude", lastLoc.longitude);
        location.put("timestamp", lastLoc.timestamp);

        return List.of(location);
    }

    /**
     * Clears location history for user
     */
    public void clearLocationHistory(String userId) {
        userLastLocation.remove(userId);
        log.info("Cleared location history for user: {}", userId);
    }

    /**
     * Gets risk score for specific country
     */
    public int getCountryRiskScore(String countryCode) {
        return calculateCountryRisk(countryCode);
    }

    /**
     * Location information holder
     */
    private static class LocationInfo {
        String ipAddress;
        double latitude;
        double longitude;
        String country;
        Instant timestamp;
    }
}
