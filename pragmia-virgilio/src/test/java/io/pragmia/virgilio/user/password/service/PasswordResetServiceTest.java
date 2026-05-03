
package io.pragmia.virgilio.user.password.service;

import io.pragmia.kernel.audit.AuditEventPublisher;
import io.pragmia.virgilio.user.UserRepository;
import io.pragmia.virgilio.user.model.VirgilioUser;
import io.pragmia.virgilio.user.password.model.PasswordPolicy;
import io.pragmia.virgilio.user.password.model.PasswordResetToken;
import io.pragmia.virgilio.user.password.repository.PasswordResetTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    @Mock UserRepository userRepo;
    @Mock PasswordResetTokenRepository resetTokenRepo;
    @Mock PasswordPolicyService policyService;
    @Mock AuditEventPublisher auditPublisher;
    @InjectMocks PasswordResetService service;

    private VirgilioUser testUser;
    private PasswordPolicy policy;

    @BeforeEach
    void setUp() throws Exception {
        var encoderField = PasswordResetService.class.getDeclaredField("passwordEncoder");
        encoderField.setAccessible(false);
        // Non serve encoder qui, lo inietta Spring in produzione

        testUser = new VirgilioUser();
        testUser.setId(UUID.randomUUID());
        testUser.setEmail("roberto@example.com");
        testUser.setPasswordHash(new BCryptPasswordEncoder(4).encode("OldPass@2025!"));

        policy = new PasswordPolicy();
        when(policyService.getActivePolicy()).thenReturn(policy);
    }

    @Test
    void initiateReset_existingEmail_shouldReturnToken() {
        when(userRepo.findByEmail("roberto@example.com")).thenReturn(Optional.of(testUser));
        when(resetTokenRepo.save(any())).thenAnswer(i -> i.getArgument(0));

        String token = service.initiateReset("roberto@example.com", "127.0.0.1");

        assertThat(token).isNotNull().hasSize(43); // 32 byte Base64url no-padding
        verify(resetTokenRepo).invalidateAllByUserId(eq(testUser.getId()), any());
        verify(resetTokenRepo).save(any());
        verify(auditPublisher).publish(any());
    }

    @Test
    void initiateReset_unknownEmail_shouldReturnNull() {
        when(userRepo.findByEmail("unknown@example.com")).thenReturn(Optional.empty());
        String token = service.initiateReset("unknown@example.com", "127.0.0.1");
        assertThat(token).isNull();
        verify(resetTokenRepo, never()).save(any());
    }

    @Test
    void validateToken_validToken_shouldReturnTrue() throws Exception {
        String plain = "test-token-value-abcde12345678901234";
        String hash  = sha256(plain);
        PasswordResetToken rt = PasswordResetToken.builder()
            .tokenHash(hash).userId(testUser.getId())
            .expiresAt(Instant.now().plusSeconds(1800)).used(false).build();
        when(resetTokenRepo.findByTokenHash(hash)).thenReturn(Optional.of(rt));

        assertThat(service.validateToken(plain)).isTrue();
    }

    @Test
    void validateToken_expiredToken_shouldReturnFalse() throws Exception {
        String plain = "expired-token-abcde123456789012345";
        String hash  = sha256(plain);
        PasswordResetToken rt = PasswordResetToken.builder()
            .tokenHash(hash).userId(testUser.getId())
            .expiresAt(Instant.now().minusSeconds(1)).used(false).build();
        when(resetTokenRepo.findByTokenHash(hash)).thenReturn(Optional.of(rt));

        assertThat(service.validateToken(plain)).isFalse();
    }

    private String sha256(String input) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        return HexFormat.of().formatHex(md.digest(input.getBytes(StandardCharsets.UTF_8)));
    }
}
