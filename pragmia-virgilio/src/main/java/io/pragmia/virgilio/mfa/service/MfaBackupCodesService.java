package io.pragmia.virgilio.mfa.service;

import io.pragmia.virgilio.mfa.model.MfaBackupCode;
import io.pragmia.virgilio.mfa.repository.MfaBackupCodeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MfaBackupCodesService {

    private final MfaBackupCodeRepository backupCodeRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom = new SecureRandom();

    private static final int BACKUP_CODE_LENGTH = 8;
    private static final int DEFAULT_BACKUP_CODES_COUNT = 10;
    private static final String ALLOWED_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // No ambiguous chars

    /**
     * Generates backup codes for a user
     */
    @Transactional
    public List<String> generateBackupCodes(String userId) {
        return generateBackupCodes(userId, DEFAULT_BACKUP_CODES_COUNT);
    }

    /**
     * Generates a specified number of backup codes for a user
     */
    @Transactional
    public List<String> generateBackupCodes(String userId, int count) {
        // Invalidate existing backup codes
        invalidateAllBackupCodes(userId);

        List<String> plainCodes = new ArrayList<>();
        Instant now = Instant.now();

        for (int i = 0; i < count; i++) {
            String plainCode = generateRandomCode();
            String hashedCode = passwordEncoder.encode(plainCode);

            MfaBackupCode backupCode = new MfaBackupCode();
            backupCode.setUserId(userId);
            backupCode.setCodeHash(hashedCode);
            backupCode.setUsed(false);
            backupCode.setCreatedAt(now);
            backupCode.setExpiresAt(now.plusSeconds(31536000)); // 1 year

            backupCodeRepository.save(backupCode);
            plainCodes.add(formatCode(plainCode));
        }

        log.info("Generated {} backup codes for user: {}", count, userId);
        return plainCodes;
    }

    /**
     * Verifies a backup code and marks it as used
     */
    @Transactional
    public boolean verifyAndConsumeBackupCode(String userId, String code) {
        String cleanCode = code.replaceAll("[^A-Z0-9]", "");

        List<MfaBackupCode> backupCodes = backupCodeRepository.findByUserIdAndUsed(userId, false);

        for (MfaBackupCode backupCode : backupCodes) {
            // Check if expired
            if (backupCode.getExpiresAt().isBefore(Instant.now())) {
                continue;
            }

            // Verify code
            if (passwordEncoder.matches(cleanCode, backupCode.getCodeHash())) {
                // Mark as used
                backupCode.setUsed(true);
                backupCode.setUsedAt(Instant.now());
                backupCodeRepository.save(backupCode);

                log.info("Backup code verified and consumed for user: {}", userId);
                return true;
            }
        }

        log.warn("Invalid backup code for user: {}", userId);
        return false;
    }

    /**
     * Gets remaining backup codes count for a user
     */
    public int getRemainingBackupCodesCount(String userId) {
        List<MfaBackupCode> codes = backupCodeRepository.findByUserIdAndUsed(userId, false);
        
        // Filter out expired codes
        Instant now = Instant.now();
        return (int) codes.stream()
                .filter(code -> code.getExpiresAt().isAfter(now))
                .count();
    }

    /**
     * Checks if user has backup codes
     */
    public boolean hasBackupCodes(String userId) {
        return getRemainingBackupCodesCount(userId) > 0;
    }

    /**
     * Invalidates all backup codes for a user
     */
    @Transactional
    public void invalidateAllBackupCodes(String userId) {
        List<MfaBackupCode> codes = backupCodeRepository.findByUserId(userId);
        backupCodeRepository.deleteAll(codes);
        log.info("Invalidated all backup codes for user: {}", userId);
    }

    /**
     * Gets backup codes statistics for a user
     */
    public Map<String, Object> getBackupCodesStats(String userId) {
        List<MfaBackupCode> allCodes = backupCodeRepository.findByUserId(userId);
        Instant now = Instant.now();

        long totalGenerated = allCodes.size();
        long activeCount = allCodes.stream()
                .filter(code -> !code.isUsed() && code.getExpiresAt().isAfter(now))
                .count();
        long usedCount = allCodes.stream()
                .filter(MfaBackupCode::isUsed)
                .count();
        long expiredCount = allCodes.stream()
                .filter(code -> !code.isUsed() && code.getExpiresAt().isBefore(now))
                .count();

        Optional<Instant> lastGenerated = allCodes.stream()
                .map(MfaBackupCode::getCreatedAt)
                .max(Instant::compareTo);

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalGenerated", totalGenerated);
        stats.put("activeCount", activeCount);
        stats.put("usedCount", usedCount);
        stats.put("expiredCount", expiredCount);
        stats.put("lastGenerated", lastGenerated.orElse(null));

        return stats;
    }

    /**
     * Regenerates backup codes (invalidates old and creates new)
     */
    @Transactional
    public List<String> regenerateBackupCodes(String userId) {
        log.info("Regenerating backup codes for user: {}", userId);
        return generateBackupCodes(userId);
    }

    /**
     * Validates backup code format without consuming it
     */
    public boolean isValidCodeFormat(String code) {
        if (code == null) {
            return false;
        }
        String cleanCode = code.replaceAll("[^A-Z0-9]", "");
        return cleanCode.length() == BACKUP_CODE_LENGTH &&
                cleanCode.chars().allMatch(c -> ALLOWED_CHARS.indexOf(c) >= 0);
    }

    /**
     * Gets list of used backup codes for audit
     */
    public List<Map<String, Object>> getUsedBackupCodes(String userId) {
        return backupCodeRepository.findByUserIdAndUsed(userId, true).stream()
                .map(code -> {
                    Map<String, Object> info = new HashMap<>();
                    info.put("id", code.getId());
                    info.put("usedAt", code.getUsedAt());
                    info.put("createdAt", code.getCreatedAt());
                    return info;
                })
                .collect(Collectors.toList());
    }

    /**
     * Generates a random backup code
     */
    private String generateRandomCode() {
        StringBuilder code = new StringBuilder(BACKUP_CODE_LENGTH);
        for (int i = 0; i < BACKUP_CODE_LENGTH; i++) {
            int index = secureRandom.nextInt(ALLOWED_CHARS.length());
            code.append(ALLOWED_CHARS.charAt(index));
        }
        return code.toString();
    }

    /**
     * Formats code with dashes for readability (XXXX-XXXX)
     */
    private String formatCode(String code) {
        if (code.length() != BACKUP_CODE_LENGTH) {
            return code;
        }
        return code.substring(0, 4) + "-" + code.substring(4);
    }

    /**
     * Cleans expired backup codes
     */
    @Transactional
    public void cleanExpiredBackupCodes() {
        Instant now = Instant.now();
        List<MfaBackupCode> allCodes = backupCodeRepository.findAll();
        
        List<MfaBackupCode> expiredCodes = allCodes.stream()
                .filter(code -> code.getExpiresAt().isBefore(now))
                .collect(Collectors.toList());
        
        if (!expiredCodes.isEmpty()) {
            backupCodeRepository.deleteAll(expiredCodes);
            log.info("Cleaned {} expired backup codes", expiredCodes.size());
        }
    }

    /**
     * Sends backup codes to user (email/download)
     */
    public void sendBackupCodesToUser(String userId, List<String> codes) {
        // TODO: Implement email service integration
        log.info("Backup codes generated for user: {}. Codes should be sent via secure channel.", userId);
    }
}
