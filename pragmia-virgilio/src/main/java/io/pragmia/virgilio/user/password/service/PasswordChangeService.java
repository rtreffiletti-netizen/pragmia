
package io.pragmia.virgilio.user.password.service;

import io.pragmia.api.audit.AuditEvent;
import io.pragmia.api.audit.AuditEventType;
import io.pragmia.kernel.audit.AuditEventPublisher;
import io.pragmia.virgilio.user.UserRepository;
import io.pragmia.virgilio.user.model.VirgilioUser;
import io.pragmia.virgilio.user.password.model.PasswordPolicy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Cambio password da parte dell'utente autenticato.
 * Richiede la password attuale come conferma identità.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordChangeService {

    private final UserRepository userRepository;
    private final PasswordPolicyService policyService;
    private final PasswordEncoder passwordEncoder;
    private final AuditEventPublisher auditPublisher;

    /**
     * Cambia la password di un utente autenticato.
     *
     * @param userId          ID utente autenticato (da SecurityContext)
     * @param currentPassword password attuale (verifica identità)
     * @param newPassword     nuova password
     * @throws IllegalArgumentException se la password attuale è errata o la nuova non rispetta la policy
     */
    @Transactional
    public void changePassword(UUID userId, String currentPassword, String newPassword) {
        VirgilioUser user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("Utente non trovato"));

        // Verifica password attuale
        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            auditPublisher.publish(new AuditEvent(
                UUID.randomUUID().toString(), AuditEventType.AUTH_FAILURE,
                Instant.now(), userId.toString(), null, null, null, null,
                userId.toString(), "PASSWORD_CHANGE", "FAILURE", "Password attuale errata",
                Map.of()
            ));
            throw new IllegalArgumentException("La password attuale non è corretta");
        }

        PasswordPolicy policy = policyService.getActivePolicy();

        // Validazione policy sulla nuova password
        List<String> violations = policyService.validate(newPassword, policy);
        if (!violations.isEmpty()) {
            throw new IllegalArgumentException("Password non valida: " + String.join("; ", violations));
        }

        // Controllo history
        if (policyService.isInHistory(userId, newPassword, policy.getHistoryCount())) {
            throw new IllegalArgumentException(
                "La password è già stata usata di recente. Scegli una password diversa.");
        }

        // Aggiorna
        String newHash = passwordEncoder.encode(newPassword);
        user.setPasswordHash(newHash);
        userRepository.save(user);

        policyService.addToHistory(userId, newHash, policy.getHistoryCount());

        auditPublisher.publish(new AuditEvent(
            UUID.randomUUID().toString(), AuditEventType.ADMIN_USER_UPDATED,
            Instant.now(), userId.toString(), null, null, null, null,
            userId.toString(), "PASSWORD_CHANGED", "SUCCESS", null,
            Map.of("userId", userId.toString())
        ));

        log.info("[PASSWORD] Password cambiata con successo per userId={}", userId);
    }

    /**
     * Forza il cambio password di un utente (operazione admin).
     * Non richiede la password attuale.
     */
    @Transactional
    public void adminForceReset(UUID userId, String newPassword, UUID adminId) {
        VirgilioUser user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("Utente non trovato"));

        PasswordPolicy policy = policyService.getActivePolicy();
        List<String> violations = policyService.validate(newPassword, policy);
        if (!violations.isEmpty()) {
            throw new IllegalArgumentException("Password non valida: " + String.join("; ", violations));
        }

        String newHash = passwordEncoder.encode(newPassword);
        user.setPasswordHash(newHash);
        userRepository.save(user);

        policyService.addToHistory(userId, newHash, policy.getHistoryCount());

        auditPublisher.publish(new AuditEvent(
            UUID.randomUUID().toString(), AuditEventType.ADMIN_USER_UPDATED,
            Instant.now(), userId.toString(), adminId != null ? adminId.toString() : null,
            null, null, null, userId.toString(), "ADMIN_PASSWORD_FORCE_RESET", "SUCCESS", null,
            Map.of("targetUserId", userId.toString())
        ));

        log.warn("[PASSWORD] Cambio forzato da admin={} per userId={}", adminId, userId);
    }
}
