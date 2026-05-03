
package io.pragmia.virgilio.user.password.service;

import io.pragmia.api.audit.AuditEvent;
import io.pragmia.api.audit.AuditEventType;
import io.pragmia.kernel.audit.AuditEventPublisher;
import io.pragmia.virgilio.user.UserRepository;
import io.pragmia.virgilio.user.model.VirgilioUser;
import io.pragmia.virgilio.user.password.model.PasswordPolicy;
import io.pragmia.virgilio.user.password.model.PasswordResetToken;
import io.pragmia.virgilio.user.password.repository.PasswordResetTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Self-service password reset.
 * Flusso: richiesta reset → token via email → validazione → nuova password.
 *
 * Il token in chiaro non viene mai persistito — viene salvato solo il suo SHA-256.
 * L'email viene delegata all'evento di audit: un listener esterno (future
 * NotificationModule) ascolta PASSWORD_RESET_REQUESTED e invia il messaggio.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository resetTokenRepository;
    private final PasswordPolicyService policyService;
    private final PasswordEncoder passwordEncoder;
    private final AuditEventPublisher auditPublisher;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * Fase 1 — Richiesta reset.
     * Genera un token sicuro e lo associa all'utente.
     * NON rivela se l'email esiste o meno (anti-enumeration).
     *
     * @param email     indirizzo email dichiarato dall'utente
     * @param requestIp IP della richiesta
     * @return il token in chiaro DA INVIARE via email (non persistito)
     *         oppure null se l'utente non esiste (la risposta HTTP sarà identica)
     */
    @Transactional
    public String initiateReset(String email, String requestIp) {
        PasswordPolicy policy = policyService.getActivePolicy();

        return userRepository.findByEmail(email).map(user -> {
            // Invalida tutti i token reset precedenti dell'utente
            resetTokenRepository.invalidateAllByUserId(user.getId(), Instant.now());

            // Genera token crittograficamente sicuro (32 byte = 256 bit → 44 char Base64)
            byte[] tokenBytes = new byte[32];
            SECURE_RANDOM.nextBytes(tokenBytes);
            String plainToken = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
            String tokenHash  = sha256(plainToken);

            Instant now = Instant.now();
            PasswordResetToken resetToken = PasswordResetToken.builder()
                .userId(user.getId())
                .tokenHash(tokenHash)
                .createdAt(now)
                .expiresAt(now.plusSeconds((long) policy.getResetTokenTtlMinutes() * 60))
                .requestIp(requestIp)
                .build();
            resetTokenRepository.save(resetToken);

            auditPublisher.publish(new AuditEvent(
                UUID.randomUUID().toString(), AuditEventType.ADMIN_USER_UPDATED,
                now, user.getId().toString(), null, null, null, requestIp,
                user.getId().toString(), "PASSWORD_RESET_REQUESTED", "SUCCESS", null,
                Map.of("email", email, "tokenExpiresAt", resetToken.getExpiresAt().toString())
            ));

            log.info("[PASSWORD-RESET] Token generato per userId={} ip={}", user.getId(), requestIp);
            return plainToken;
        }).orElseGet(() -> {
            // Timing costante: esegui comunque la hash per non rivelare l'assenza dell'utente
            sha256("dummy-constant-timing-" + email);
            log.debug("[PASSWORD-RESET] Email non trovata — risposta identica per sicurezza");
            return null;
        });
    }

    /**
     * Fase 2 — Validazione token.
     * Verifica che il token sia valido senza consumarlo.
     *
     * @return true se il token è valido e non scaduto
     */
    @Transactional(readOnly = true)
    public boolean validateToken(String plainToken) {
        String hash = sha256(plainToken);
        return resetTokenRepository.findByTokenHash(hash)
            .map(PasswordResetToken::isValid)
            .orElse(false);
    }

    /**
     * Fase 3 — Conferma reset con nuova password.
     * Valida il token, applica la policy, aggiorna la password, invalida il token.
     *
     * @throws IllegalArgumentException se il token non è valido o la password non rispetta la policy
     */
    @Transactional
    public void confirmReset(String plainToken, String newPassword, String requestIp) {
        String hash = sha256(plainToken);

        PasswordResetToken resetToken = resetTokenRepository.findByTokenHash(hash)
            .orElseThrow(() -> new IllegalArgumentException("Token non valido o scaduto"));

        if (!resetToken.isValid()) {
            throw new IllegalArgumentException("Token non valido o scaduto");
        }

        VirgilioUser user = userRepository.findById(resetToken.getUserId())
            .orElseThrow(() -> new IllegalArgumentException("Utente non trovato"));

        // Validazione policy
        PasswordPolicy policy = policyService.getActivePolicy();
        List<String> violations = policyService.validate(newPassword, policy);
        if (!violations.isEmpty()) {
            throw new IllegalArgumentException("Password non valida: " + String.join("; ", violations));
        }

        // Controllo history
        if (policyService.isInHistory(user.getId(), newPassword, policy.getHistoryCount())) {
            throw new IllegalArgumentException(
                "La password è già stata usata di recente. Scegli una password diversa.");
        }

        // Aggiorna password
        String newHash = passwordEncoder.encode(newPassword);
        user.setPasswordHash(newHash);
        userRepository.save(user);

        // Aggiorna history
        policyService.addToHistory(user.getId(), newHash, policy.getHistoryCount());

        // Invalida il token (monouso)
        resetToken.setUsed(true);
        resetToken.setUsedAt(Instant.now());
        resetTokenRepository.save(resetToken);

        auditPublisher.publish(new AuditEvent(
            UUID.randomUUID().toString(), AuditEventType.ADMIN_USER_UPDATED,
            Instant.now(), user.getId().toString(), null, null, null, requestIp,
            user.getId().toString(), "PASSWORD_RESET_CONFIRMED", "SUCCESS", null,
            Map.of("userId", user.getId().toString())
        ));

        log.info("[PASSWORD-RESET] Password aggiornata con successo per userId={}", user.getId());
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 non disponibile", e);
        }
    }
}
