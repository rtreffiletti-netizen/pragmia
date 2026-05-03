package io.pragmia.saml.service;

import io.pragmia.saml.config.SamlProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Signature;
import java.util.Base64;

/**
 * Gestisce firma e verifica della firma digitale sulle SAML assertion.
 * Usa il keystore configurato in SamlProperties.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SamlSignatureService {

    private final SamlProperties samlProperties;

    /**
     * Firma il payload XML con RSA-SHA256 e restituisce la firma Base64.
     */
    public String sign(String xmlPayload) {
        try {
            PrivateKey privateKey = loadPrivateKey();
            Signature sig = Signature.getInstance("SHA256withRSA");
            sig.initSign(privateKey);
            sig.update(xmlPayload.getBytes(StandardCharsets.UTF_8));
            byte[] signatureBytes = sig.sign();
            return Base64.getEncoder().encodeToString(signatureBytes);
        } catch (Exception e) {
            log.error("[PRAGMIA-SAML] Errore durante la firma: {}", e.getMessage());
            throw new RuntimeException("Firma SAML fallita", e);
        }
    }

    /**
     * Verifica la firma Base64 su un payload XML con il certificato pubblico dell'SP.
     */
    public boolean verify(String xmlPayload, String signatureBase64, java.security.cert.Certificate cert) {
        try {
            Signature sig = Signature.getInstance("SHA256withRSA");
            sig.initVerify(cert.getPublicKey());
            sig.update(xmlPayload.getBytes(StandardCharsets.UTF_8));
            return sig.verify(Base64.getDecoder().decode(signatureBase64));
        } catch (Exception e) {
            log.warn("[PRAGMIA-SAML] Verifica firma fallita: {}", e.getMessage());
            return false;
        }
    }

    private PrivateKey loadPrivateKey() throws Exception {
        SamlProperties.Idp idp = samlProperties.getIdp();
        String keystorePath = idp.getKeystorePath();

        InputStream ks;
        if (keystorePath.startsWith("classpath:")) {
            String cp = keystorePath.substring("classpath:".length());
            ks = getClass().getClassLoader().getResourceAsStream(cp);
        } else {
            ks = new java.io.FileInputStream(keystorePath);
        }

        KeyStore keyStore = KeyStore.getInstance("JKS");
        keyStore.load(ks, idp.getKeystorePassword().toCharArray());
        return (PrivateKey) keyStore.getKey(idp.getKeyAlias(), idp.getKeyPassword().toCharArray());
    }
}
