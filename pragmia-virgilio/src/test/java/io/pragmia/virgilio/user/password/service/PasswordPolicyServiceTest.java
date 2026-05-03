
package io.pragmia.virgilio.user.password.service;

import io.pragmia.virgilio.user.password.model.PasswordHistory;
import io.pragmia.virgilio.user.password.model.PasswordPolicy;
import io.pragmia.virgilio.user.password.repository.PasswordHistoryRepository;
import io.pragmia.virgilio.user.password.repository.PasswordPolicyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordPolicyServiceTest {

    @Mock PasswordPolicyRepository policyRepo;
    @Mock PasswordHistoryRepository historyRepo;
    @InjectMocks PasswordPolicyService service;

    private final PasswordEncoder encoder = new BCryptPasswordEncoder(4);
    private PasswordPolicy defaultPolicy;

    @BeforeEach
    void setUp() throws Exception {
        // Inject encoder reale (BCrypt cost 4 per velocità nei test)
        var f = PasswordPolicyService.class.getDeclaredField("passwordEncoder");
        f.setAccessible(true);
        f.set(service, encoder);

        defaultPolicy = new PasswordPolicy();
        when(policyRepo.findById("default")).thenReturn(Optional.of(defaultPolicy));
    }

    @Test
    void validate_validPassword_shouldReturnEmpty() {
        List<String> violations = service.validate("Pragmia@2026!", defaultPolicy);
        assertThat(violations).isEmpty();
    }

    @Test
    void validate_tooShort_shouldReturnViolation() {
        List<String> v = service.validate("Ab1!", defaultPolicy);
        assertThat(v).anyMatch(s -> s.contains("almeno"));
    }

    @Test
    void validate_missingUppercase_shouldReturnViolation() {
        List<String> v = service.validate("pragmia@2026!", defaultPolicy);
        assertThat(v).anyMatch(s -> s.contains("maiuscola"));
    }

    @Test
    void validate_missingSpecial_shouldReturnViolation() {
        List<String> v = service.validate("Pragmia20261234", defaultPolicy);
        assertThat(v).anyMatch(s -> s.contains("speciale"));
    }

    @Test
    void isInHistory_passwordNotInHistory_shouldReturnFalse() {
        UUID userId = UUID.randomUUID();
        when(historyRepo.findLatestByUserId(eq(userId), any())).thenReturn(List.of());
        assertThat(service.isInHistory(userId, "NewPass@123", 5)).isFalse();
    }

    @Test
    void isInHistory_passwordInHistory_shouldReturnTrue() {
        UUID userId = UUID.randomUUID();
        String plainPwd = "OldPass@2025!";
        String hash = encoder.encode(plainPwd);
        PasswordHistory hist = PasswordHistory.builder().userId(userId).passwordHash(hash).build();
        when(historyRepo.findLatestByUserId(eq(userId), any())).thenReturn(List.of(hist));

        assertThat(service.isInHistory(userId, plainPwd, 5)).isTrue();
    }

    @Test
    void getActivePolicy_whenNotExists_shouldCreateDefault() {
        when(policyRepo.findById("default")).thenReturn(Optional.empty());
        when(policyRepo.save(any())).thenAnswer(i -> i.getArgument(0));
        PasswordPolicy p = service.getActivePolicy();
        assertThat(p).isNotNull();
        assertThat(p.getMinLength()).isEqualTo(12);
    }
}
