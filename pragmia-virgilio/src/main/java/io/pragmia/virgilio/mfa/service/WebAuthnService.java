package io.pragmia.virgilio.mfa.service;

import com.webauthn4j.WebAuthnManager;
import com.webauthn4j.WebAuthnManagerBuilder;
import com.webauthn4j.data.*;
import com.webauthn4j.data.client.Origin;
import com.webauthn4j.data.extension.client.RegistrationExtensionClientInput;
import com.webauthn4j.data.extension.authenticator.*;
import com.webauthn4j.converter.util.ObjectConverter;
import io.pragmia.virgilio.user.model.VirgilioUser;
import io.pragmia.virgilio.mfa.model.WebAuthnCredential;
import io.pragmia.virgilio.mfa.repository.WebAuthnCredentialRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebAuthnService {

    private final WebAuthnCredentialRepository repo;
    private final WebAuthnManager waManager = new WebAuthnManagerBuilder().build();
    private final static byte[] RP_ID_BYTES = "localhost".getBytes(StandardCharsets.UTF_8);
    private final static String RP_NAME = "PRAGMIA";

    public RegistrationOptions generateRegistrationOptions(String username, boolean userless) {
        PublicKeyCredentialUserEntity user = new PublicKeyCredentialUserEntity(
            username.getBytes(StandardCharsets.UTF_8), username, username);
        AuthenticatorSelectionCriteria selection = AuthenticatorSelectionCriteria.builder()
            .setAuthenticatorAttachment(AuthenticatorAttachment.PLATFORM)
            .setResidentKeyRequirement(userless ? ResidentKeyRequirement.REQUIRED : ResidentKeyRequirement.PREFERRED)
            .setUserVerificationRequirement(UserVerificationRequirement.REQUIRED)
            .build();
        RelyingParty rp = new RelyingParty(new String(RP_ID_BYTES, StandardCharsets.UTF_8), RP_NAME, "");
        return new RegistrationOptions(PublicKeyCredentialCreationOptions.builder()
            .rp(rp)
            .user(user)
            .challenge(java.util.Base64.getDecoder().decode("dGVzdA=="))
            .pubKeyCredParams(List.of(new PublicKeyCredentialParameters(PublicKeyCredentialType.PUBLIC_KEY, 257)))
            .authenticatorSelection(selection)
            .attestation(AttestationConveyancePreference.NONE)
            .build());
    }

    @Transactional
    public void registerCredential(String username, String credentialIdB64, String publicKeyB64) {
        WebAuthnCredential cred = new WebAuthnCredential();
        cred.setCredentialId(Base64.getDecoder().decode(credentialIdB64));
        cred.setPublicKey(Base64.getDecoder().decode(publicKeyB64));
        cred.setCounter(0);
        cred.setBackupEligible(true);
        cred.setAuthenticatorType("platform");
        cred.setCreatededAt(Instant.now());
        cred.setUserless(false);
        cred.setLastUsedAt(null);
    }

    @Transactional
    public void deleteCredential(UUID credId) {
        repo.deleteById(credId);
    }

    @Transactional
    public void deleteAllForUser(UUID userId) {
        repo.deleteByUserId(userId);
    }

    public List<WebAuthnCredential> listCredentials(UUID userId) {
        return repo.findByUserId(userId);
    }

    public AuthenticationOptions generateAuthenticationOptions(boolean userless) {
        PublicKeyCredentialRequestOptions.Builder builder = PublicKeyCredentialRequestOptions.builder()
            .challenge(java.util.Base64.getEncoder().encode(Instant.now().toString().getBytes()));
        if (!userless) builder.allowList(List.of());
        return new AuthenticationOptions(builder.build());
    }

    public AuthenticationResult authenticate(UUID userId, String responseB64) {
        AuthenticationOptions opts = generateAuthenticationOptions(false);
        return waManager.parse(Base64.getDecoder().decode(responseB64),
            opts.getPublicKeyCredentialRequestOptions(),
            new Origin("https://localhost:8080"));
    }

    public AuthenticationResult authenticateUserless(String responseB64) {
        AuthenticationOptions opts = generateAuthenticationOptions(true);
        return waManager.parse(Base64.getDecoder().decode(responseB64),
            opts.getPublicKeyCredentialRequestOptions(),
            new Origin("https://localhost:8080"));
    }
}
