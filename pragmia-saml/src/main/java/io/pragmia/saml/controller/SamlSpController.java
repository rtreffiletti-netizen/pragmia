package io.pragmia.saml.controller;

import io.pragmia.saml.service.SamlMetadataService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * PRAGMIA come Service Provider SAML2.
 * Consuma assertion da IdP esterni (Azure AD, ADFS, SPID, CIE).
 * Il flusso ACS è gestito da Spring Security SAML2 — qui solo endpoint di supporto.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@ConditionalOnProperty(name = "pragmia.modules.saml.enabled", havingValue = "true", matchIfMissing = true)
public class SamlSpController {

    private final SamlMetadataService metadataService;

    /**
     * Metadata dell'SP per un determinato registrationId.
     * L'IdP esterno lo usa per configurare la trust federation.
     */
    @GetMapping(value = "/saml/sp/{registrationId}/metadata", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<String> spMetadata(@PathVariable String registrationId,
                                             HttpServletRequest request) {
        String baseUrl = request.getScheme() + "://" + request.getServerName()
            + (request.getServerPort() != 80 && request.getServerPort() != 443
                ? ":" + request.getServerPort() : "");
        log.info("[PRAGMIA-SAML] Metadata SP richiesto per registrationId={}", registrationId);
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_XML)
            .body(metadataService.generateSpMetadata(registrationId, baseUrl));
    }

    /**
     * Landing page post-login SP.
     * Dopo il login SAML2 da IdP esterno, Spring Security redirige qui.
     * In produzione: emette un token PRAGMIA (OAuth2/OIDC) e redirige alla SPA.
     */
    @GetMapping("/saml/sp/post-login")
    public ResponseEntity<String> postLogin(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            log.info("[PRAGMIA-SAML] Login SP completato per utente: {}", authentication.getName());
            return ResponseEntity.ok("Login completato per: " + authentication.getName());
        }
        return ResponseEntity.status(401).body("Non autenticato");
    }
}
