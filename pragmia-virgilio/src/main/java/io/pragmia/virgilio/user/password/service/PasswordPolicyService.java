
package io.pragmia.virgilio.user.password.service;

import io.pragmia.virgilio.user.password.model.PasswordHistory;
import io.pragmia.virgilio.user.password.model.PasswordPolicy;
import io.pragmia.virgilio.user.password.repository.PasswordHistoryRepository;
import io.pragmia.virgilio.user.password.repository.PasswordPolicyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Valida le password rispetto alla policy configurata e gestisce
 * lo storico password per impedire il riutilizzo.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordPolicyService {

    private static final Pattern UPPERCASE = Pattern.compile("[A-Z]");
    private static final Pattern LOWERCASE = Pattern.compile("[a-z]");
    private static final Pattern DIGIT     = Pattern.compile("[0-9]");
    private static final Pattern SPECIAL   = Pattern.compile("[^A-Za-z0-9]");

    private final PasswordPolicyRepository policyRepository;
    private final PasswordHistoryRepository historyRepository;
    private final PasswordEncoder passwordEncoder;

    /** Carica la policy attiva (crea quella di default se non esiste) */
    public PasswordPolicy getActivePolicy() {
        return policyRepository.findById("default").orElseGet(() -> {
            PasswordPolicy p = new PasswordPolicy();
            return policyRepository.save(p);
        });
    }

    /**
     * Valida una password in chiaro rispetto alla policy.
     * @return lista di violazioni — vuota se la password è valida
     */
    public List<String> validate(String plainPassword, PasswordPolicy policy) {
        List<String> violations = new ArrayList<>();

        if (plainPassword == null || plainPassword.length() < policy.getMinLength()) {
            violations.add("La password deve essere di almeno " + policy.getMinLength() + " caratteri");
        }
        if (plainPassword != null && plainPassword.length() > policy.getMaxLength()) {
            violations.add("La password non può superare " + policy.getMaxLength() + " caratteri");
        }
        if (policy.isRequireUppercase() && !UPPERCASE.matcher(plainPassword != null ? plainPassword : "").find()) {
            violations.add("La password deve contenere almeno una lettera maiuscola");
        }
        if (policy.isRequireLowercase() && !LOWERCASE.matcher(plainPassword != null ? plainPassword : "").find()) {
            violations.add("La password deve contenere almeno una lettera minuscola");
        }
        if (policy.isRequireDigit() && !DIGIT.matcher(plainPassword != null ? plainPassword : "").find()) {
            violations.add("La password deve contenere almeno un numero");
        }
        if (policy.isRequireSpecial() && !SPECIAL.matcher(plainPassword != null ? plainPassword : "").find()) {
            violations.add("La password deve contenere almeno un carattere speciale (!@#$%...)");
        }
        return violations;
    }

    /**
     * Verifica se la nuova password è già stata usata nelle ultime N volte.
     * @return true se la password è già nella history (da rifiutare)
     */
    public boolean isInHistory(UUID userId, String plainPassword, int historyCount) {
        if (historyCount <= 0) return false;
        List<PasswordHistory> history = historyRepository.findLatestByUserId(
            userId, PageRequest.of(0, historyCount));
        return history.stream()
            .anyMatch(h -> passwordEncoder.matches(plainPassword, h.getPasswordHash()));
    }

    /**
     * Persiste la nuova password hash nella history e fa pruning automatico.
     * Da chiamare DOPO aver aggiornato la password dell'utente.
     */
    @Transactional
    public void addToHistory(UUID userId, String newPasswordHash, int historyCount) {
        PasswordHistory entry = PasswordHistory.builder()
            .userId(userId)
            .passwordHash(newPasswordHash)
            .createdAt(Instant.now())
            .build();
        historyRepository.save(entry);
        if (historyCount > 0) {
            historyRepository.pruneHistory(userId, historyCount);
        }
        log.debug("[PASSWORD] History aggiornata per userId={}", userId);
    }
}
