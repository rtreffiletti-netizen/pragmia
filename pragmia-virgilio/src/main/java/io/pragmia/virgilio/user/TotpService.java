package io.pragmia.virgilio.user;

import dev.samstevens.totp.code.*;
import dev.samstevens.totp.qr.*;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import io.pragmia.virgilio.user.model.VirgilioUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TotpService {

    private final UserRepository repo;
    private final DefaultSecretGenerator secretGen = new DefaultSecretGenerator(32);
    private final CodeVerifier verifier = new DefaultCodeVerifier(
        new DefaultCodeGenerator(), new SystemTimeProvider());

    @Transactional
    public String enrollTotp(UUID userId) {
        VirgilioUser u = repo.findById(userId).orElseThrow();
        String secret = secretGen.generate();
        u.setTotpSecret(secret);
        u.setTotpEnabled(false); // confermato solo dopo primo verify
        repo.save(u);
        return secret;
    }

    @Transactional
    public boolean verifyAndActivate(UUID userId, String code) {
        VirgilioUser u = repo.findById(userId).orElseThrow();
        if (u.getTotpSecret() == null) return false;
        boolean valid;
        try { valid = verifier.isValidCode(u.getTotpSecret(), code); }
        catch (Exception e) { return false; }
        if (valid) { u.setTotpEnabled(true); repo.save(u); }
        return valid;
    }

    public boolean verify(VirgilioUser user, String code) {
        if (!user.isTotpEnabled() || user.getTotpSecret() == null) return false;
        try { return verifier.isValidCode(user.getTotpSecret(), code); }
        catch (Exception e) { return false; }
    }

    @Transactional
    public void disableTotp(UUID userId) {
        repo.findById(userId).ifPresent(u -> {
            u.setTotpEnabled(false); u.setTotpSecret(null); repo.save(u);
        });
    }

    public String buildOtpAuthUri(String secret, String username, String issuer) {
        return String.format("otpauth://totp/%s:%s?secret=%s&issuer=%s&algorithm=SHA1&digits=6&period=30",
            issuer, username, secret, issuer);
    }
}
