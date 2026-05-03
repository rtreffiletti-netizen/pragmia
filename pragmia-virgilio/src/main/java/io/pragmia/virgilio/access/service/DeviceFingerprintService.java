package io.pragmia.virgilio.access.service;

import io.pragmia.virgilio.access.model.LoginContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Device Fingerprint Service - Integrates with RiskEngine
 * Tracks and identifies devices for risk assessment
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceFingerprintService {

    // Known devices per user (userId -> Set of device fingerprints)
    private final Map<String, Set<String>> userKnownDevices = new ConcurrentHashMap<>();
    
    // Device metadata (fingerprint -> DeviceInfo)
    private final Map<String, DeviceInfo> deviceMetadata = new ConcurrentHashMap<>();

    /**
     * Calculates device risk score (0-20)
     * Used by RiskEngine.evaluateDeviceRisk()
     */
    public int calculateDeviceRisk(LoginContext context) {
        String deviceFingerprint = context.getDeviceFingerprint();
        String userId = context.getUserId();

        if (deviceFingerprint == null || deviceFingerprint.isEmpty()) {
            log.warn("No device fingerprint provided for user: {}", userId);
            return 20; // High risk - no fingerprint
        }

        // Check if device is known
        if (isKnownDevice(userId, deviceFingerprint)) {
            updateDeviceLastSeen(deviceFingerprint);
            return 0; // Known device = no risk
        }

        // New device detected
        log.info("New device detected for user: {}", userId);
        registerDevice(userId, deviceFingerprint, context);
        return 15; // Medium-high risk for new device
    }

    /**
     * Generates device fingerprint from context
     */
    public String generateFingerprint(LoginContext context) {
        StringBuilder fpBuilder = new StringBuilder();
        
        // User Agent
        if (context.getUserAgent() != null) {
            fpBuilder.append(context.getUserAgent());
        }
        
        // Screen resolution (if available)
        fpBuilder.append("|");
        
        // Timezone
        fpBuilder.append("|");
        
        // Language
        fpBuilder.append("|");
        
        // Platform
        fpBuilder.append("|");

        // Generate hash
        return hashFingerprint(fpBuilder.toString());
    }

    /**
     * Checks if device is known for user
     */
    public boolean isKnownDevice(String userId, String deviceFingerprint) {
        Set<String> knownDevices = userKnownDevices.get(userId);
        return knownDevices != null && knownDevices.contains(deviceFingerprint);
    }

    /**
     * Registers a new device for user
     */
    public void registerDevice(String userId, String deviceFingerprint, LoginContext context) {
        // Add to user's known devices
        userKnownDevices.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet())
                .add(deviceFingerprint);

        // Store device metadata
        DeviceInfo info = new DeviceInfo();
        info.fingerprint = deviceFingerprint;
        info.userId = userId;
        info.userAgent = context.getUserAgent();
        info.firstSeen = Instant.now();
        info.lastSeen = Instant.now();
        info.loginCount = 1;
        info.trusted = false;

        deviceMetadata.put(deviceFingerprint, info);

        log.info("Device registered for user {}: {}", userId, deviceFingerprint);
    }

    /**
     * Updates device last seen timestamp
     */
    private void updateDeviceLastSeen(String deviceFingerprint) {
        DeviceInfo info = deviceMetadata.get(deviceFingerprint);
        if (info != null) {
            info.lastSeen = Instant.now();
            info.loginCount++;
        }
    }

    /**
     * Marks device as trusted
     */
    public void trustDevice(String userId, String deviceFingerprint) {
        DeviceInfo info = deviceMetadata.get(deviceFingerprint);
        if (info != null && info.userId.equals(userId)) {
            info.trusted = true;
            log.info("Device marked as trusted for user {}: {}", userId, deviceFingerprint);
        }
    }

    /**
     * Removes device from user's known devices
     */
    public void removeDevice(String userId, String deviceFingerprint) {
        Set<String> devices = userKnownDevices.get(userId);
        if (devices != null) {
            devices.remove(deviceFingerprint);
            deviceMetadata.remove(deviceFingerprint);
            log.info("Device removed for user {}: {}", userId, deviceFingerprint);
        }
    }

    /**
     * Gets all known devices for user
     */
    public List<Map<String, Object>> getUserDevices(String userId) {
        Set<String> deviceIds = userKnownDevices.get(userId);
        if (deviceIds == null || deviceIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<Map<String, Object>> devices = new ArrayList<>();
        for (String fingerprin : deviceIds) {
            DeviceInfo info = deviceMetadata.get(fingerprint);
            if (info != null) {
                Map<String, Object> device = new HashMap<>();
                device.put("fingerprint", info.fingerprint);
                device.put("userAgent", info.userAgent);
                device.put("firstSeen", info.firstSeen);
                device.put("lastSeen", info.lastSeen);
                device.put("loginCount", info.loginCount);
                device.put("trusted", info.trusted);
                device.put("browser", extractBrowser(info.userAgent));
                device.put("os", extractOS(info.userAgent));
                devices.add(device);
            }
        }

        return devices;
    }

    /**
     * Gets device count for user
     */
    public int getDeviceCount(String userId) {
        Set<String> devices = userKnownDevices.get(userId);
        return devices != null ? devices.size() : 0;
    }

    /**
     * Extracts browser from User-Agent
     */
    private String extractBrowser(String userAgent) {
        if (userAgent == null) return "Unknown";
        
        String ua = userAgent.toLowerCase();
        if (ua.contains("edg/")) return "Edge";
        if (ua.contains("chrome/")) return "Chrome";
        if (ua.contains("firefox/")) return "Firefox";
        if (ua.contains("safari/")) return "Safari";
        if (ua.contains("opera/")) return "Opera";
        
        return "Other";
    }

    /**
     * Extracts OS from User-Agent
     */
    private String extractOS(String userAgent) {
        if (userAgent == null) return "Unknown";
        
        String ua = userAgent.toLowerCase();
        if (ua.contains("windows")) return "Windows";
        if (ua.contains("mac")) return "MacOS";
        if (ua.contains("linux")) return "Linux";
        if (ua.contains("android")) return "Android";
        if (ua.contains("iphone") || ua.contains("ipad")) return "iOS";
        
        return "Other";
    }

    /**
     * Hashes fingerprint for storage
     */
    private String hashFingerprint(String fingerprint) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(fingerprint.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            log.error("Error hashing fingerprint", e);
            return fingerprint; // Fallback to plain fingerprint
        }
    }

    /**
     * Analyzes device change patterns
     */
    public boolean isFrequentDeviceChanger(String userId) {
        int deviceCount = getDeviceCount(userId);
        return deviceCount > 5; // Threshold: more than 5 devices is suspicious
    }

    /**
     * Gets device trust level (0-100)
     */
    public int getDeviceTrustLevel(String deviceFingerprint) {
        DeviceInfo info = deviceMetadata.get(deviceFingerprint);
        if (info == null) {
            return 0; // Unknown device
        }

        if (info.trusted) {
            return 100; // Explicitly trusted
        }

        // Calculate trust based on usage
        int trustScore = 50; // Base score

        // Increase trust for frequent use
        if (info.loginCount > 10) trustScore += 20;
        else if (info.loginCount > 5) trustScore += 10;

        // Increase trust for older devices
        long daysSinceFirstSeen = java.time.Duration.between(info.firstSeen, Instant.now()).toDays();
        if (daysSinceFirstSeen > 30) trustScore += 20;
        else if (daysSinceFirstSeen > 7) trustScore += 10;

        return Math.min(trustScore, 90); // Max 90 without explicit trust
    }

    /**
     * Clears all devices for user
     */
    public void clearUserDevices(String userId) {
        Set<String> devices = userKnownDevices.remove(userId);
        if (devices != null) {
            devices.forEach(deviceMetadata::remove);
            log.info("Cleared all devices for user: {}", userId);
        }
    }

    /**
     * Device information holder
     */
    private static class DeviceInfo {
        String fingerprint;
        String userId;
        String userAgent;
        Instant firstSeen;
        Instant lastSeen;
        int loginCount;
        boolean trusted;
    }
}
