package io.pragmia.virgilio.mfa.service;

import dev.samstevens.totp.code.*;
import dev.samstevens.totp.exceptions.QrGenerationException;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.qr.JdkGenerators;
import dev.samstevens.totp.secret.*;
import io.pragmia.virgilio.user.model.VirgilioUser;
import io.pragmia.virgilio.mfa.model.*;
import io.pragmia.virgilio.mfa.repository.TotpCredentialRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TotpOathService {

    private final TotpCredentialRepository repo;
    private final SecretGenerator secretGen = new DefaultSecretGenerator();
    private final TimeBasedCodeVerifier verifier = new DefaultTimeBasedCodeVerifier();
    private final TimeBasedOneTimeCodeGenerator codeGen = new DefaultTimeBasedOneTimeCodeGenerator();

    public TotpRegistration generateSecret(String username, int digits, String algorithm) {
        String secret = secretGen.generate();
        TotpCredential cred = new TotpCredential();
        cred.setSecret(secret);
        cred.setDigits(digits);
        cred.setAlgorithm(algorithm);
        cred.setPeriod(30);
        cred.setWindow(1);
        String provisioningUri = String.format("otpauth://totp/PRAGMIA:%s?secret=%s&issuer=PRAGMIA&algorithm=%s",
            username, secret, algorithm);
        return new TotpRegistration(secret, provisioningUri, generateQrCode(provisioningUri));
    }

    @Transactional
    public boolean verifyCode(VirgilioUser user, String code) {
        TotpCredential cred = repo.findByUser(user).orElse(null);
        if (cred == null || !cred.isEnabled()) return false;
        boolean valid = verifier.isValidCode(
            cred.getSecret(), code,
            new JdkTimeProvider(),
            new CodeVerifier.Algorithm.valueOf(cred.getAlgorithm()),
            cred.getDigits(), cred.getPeriod(), cred.getWindow());
        if (valid) cred.setLastVerifiedAt(java.time.Instant.now());
        return valid;
    }

    private String generateQrCode(String uri) {
        try {
            return new JdkGenerators().GoogleChartsQrGenerator().generate("PRAGMIA", uri);
        } catch (QrGenerationException e) {
            return null;
        }
    }

    public record TotpRegistration(String secret, String uri, String qrCode) {}
}
