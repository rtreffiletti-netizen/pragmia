package io.pragmia.saml.controller;

import io.pragmia.saml.config.SamlProperties;
import io.pragmia.saml.model.SamlServiceProvider;
import io.pragmia.saml.repository.SamlServiceProviderRepository;
import io.pragmia.saml.service.SamlAssertionService;
import io.pragmia.saml.service.SamlAuditService;
import io.pragmia.saml.service.SamlMetadataService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * PRAGMIA come Identity Provider SAML2.
 * Espone: /saml/idp/metadata, /saml/idp/sso, /saml/idp/slo
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@ConditionalOnProperty(name = "pragmia.modules.saml.enabled", havingValue = "true", matchIfMissing = true)
public class SamlIdpController {

    private final SamlMetadataService metadataService;
    private final SamlAssertionService assertionService;
    private final SamlAuditService auditService;
    private final SamlServiceProviderRepository spRepository;

    // ── Metadata ────────────────────────────────────────────────────────────

    @GetMapping(value = "/saml/idp/metadata", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<String> idpMetadata(HttpServletRequest request) {
        String baseUrl = getBaseUrl(request);
        auditService.logMetadataRequest(null, "PRAGMIA-IDP", request.getRemoteAddr());
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_XML)
            .body(metadataService.generateIdpMetadata(baseUrl));
    }

    // ── SSO (SP-Initiated: riceve AuthnRequest) ──────────────────────────────

    /**
     * Riceve la SAMLRequest (base64+deflate) dall'SP, autentica l'utente (già
     * gestito da VIRGILIO), costruisce la Response e fa il POST all'ACS.
     */
    @GetMapping("/saml/idp/sso")
    public void ssoRedirect(@RequestParam(required = false) String SAMLRequest,
                            @RequestParam(required = false) String RelayState,
                            Authentication authentication,
                            HttpServletRequest request,
                            HttpServletResponse response) throws Exception {
        handleSso(SAMLRequest, RelayState, authentication, request, response);
    }

    @PostMapping("/saml/idp/sso")
    public void ssoPost(@RequestParam(required = false) String SAMLRequest,
                        @RequestParam(required = false) String RelayState,
                        Authentication authentication,
                        HttpServletRequest request,
                        HttpServletResponse response) throws Exception {
        handleSso(SAMLRequest, RelayState, authentication, request, response);
    }

    private void handleSso(String samlRequestB64, String relayState,
                           Authentication authentication,
                           HttpServletRequest request,
                           HttpServletResponse response) throws Exception {

        if (authentication == null || !authentication.isAuthenticated()) {
            response.sendRedirect("/login?samlRequest=" + samlRequestB64
                + (relayState != null ? "&RelayState=" + relayState : ""));
            return;
        }

        // Decodifica AuthnRequest per ricavare entityId SP e ACS
        String spEntityId  = extractEntityIdFromRequest(samlRequestB64);
        String inResponseTo = extractInResponseTo(samlRequestB64);

        SamlServiceProvider sp = spRepository.findByEntityId(spEntityId)
            .orElseThrow(() -> new IllegalArgumentException("SP non registrato: " + spEntityId));

        String userId = authentication.getName();
        String email  = userId; // In produzione: risolvi da UserRepository

        Map<String, List<String>> attrs = new HashMap<>();
        attrs.put("email", List.of(email));
        // In produzione: carica attributi aggiuntivi dal profilo utente

        String samlResponseXml = assertionService.buildSignedResponse(
            userId, email, sp, attrs, inResponseTo);
        String samlResponseB64 = Base64.getEncoder()
            .encodeToString(samlResponseXml.getBytes(StandardCharsets.UTF_8));

        auditService.logSsoSuccess(userId, sp.getEntityId(), request.getRemoteAddr());

        // POST binding: risposta via HTML form auto-submit
        response.setContentType("text/html;charset=UTF-8");
        String html = buildAutoPostForm(sp.getAcsUrl(), samlResponseB64, relayState);
        response.getWriter().write(html);
    }

    // ── SLO ─────────────────────────────────────────────────────────────────

    @PostMapping("/saml/idp/slo")
    public void sloPost(@RequestParam(required = false) String SAMLRequest,
                        Authentication authentication,
                        HttpServletRequest request,
                        HttpServletResponse response) throws Exception {
        if (authentication != null) {
            auditService.logSloRequest(authentication.getName(), null, request.getRemoteAddr());
        }
        // Invalida sessione HTTP e SAML sessions
        request.getSession().invalidate();
        response.sendRedirect("/login?logout");
    }

    // ── IdP-Initiated SSO ───────────────────────────────────────────────────

    /**
     * IdP-Initiated: l'admin avvia l'SSO verso un SP senza AuthnRequest.
     * Usato per portali enterprise e link rapidi.
     */
    @PostMapping("/api/saml/idp/initiate")
    public void initiateIdpSso(@RequestParam String spEntityId,
                               Authentication authentication,
                               HttpServletRequest request,
                               HttpServletResponse response) throws Exception {

        SamlServiceProvider sp = spRepository.findByEntityId(spEntityId)
            .orElseThrow(() -> new IllegalArgumentException("SP non registrato: " + spEntityId));

        if (!"idp-initiated".equals(sp.getAllowedFlow()) && !"both".equals(sp.getAllowedFlow())) {
            response.sendError(403, "IdP-initiated non consentito per questo SP");
            return;
        }

        String userId = authentication.getName();
        Map<String, List<String>> attrs = new HashMap<>();
        attrs.put("email", List.of(userId));

        String samlResponseXml = assertionService.buildSignedResponse(userId, userId, sp, attrs, null);
        String samlResponseB64 = Base64.getEncoder()
            .encodeToString(samlResponseXml.getBytes(StandardCharsets.UTF_8));

        response.setContentType("text/html;charset=UTF-8");
        response.getWriter().write(buildAutoPostForm(sp.getAcsUrl(), samlResponseB64, null));
    }

    // ── Helpers ─────────────────────────────────────────────────────────────

    private String buildAutoPostForm(String acsUrl, String samlResponse, String relayState) {
        StringBuilder form = new StringBuilder();
        form.append("<!DOCTYPE html><html><body onload=\"document.forms[0].submit()\">")
            .append("<form method=\"POST\" action=\"").append(acsUrl).append("\">")
            .append("<input type=\"hidden\" name=\"SAMLResponse\" value=\"").append(samlResponse).append("\"/>");
        if (relayState != null) {
            form.append("<input type=\"hidden\" name=\"RelayState\" value=\"").append(relayState).append("\"/>");
        }
        form.append("<noscript><button type=\"submit\">Continua</button></noscript>")
            .append("</form></body></html>");
        return form.toString();
    }

    private String extractEntityIdFromRequest(String samlRequestB64) {
        if (samlRequestB64 == null) return "unknown";
        try {
            String decoded = new String(Base64.getDecoder().decode(samlRequestB64), StandardCharsets.UTF_8);
            int start = decoded.indexOf("Issuer>");
            int end   = decoded.indexOf("</saml:Issuer>");
            if (start > 0 && end > start) return decoded.substring(start + 7, end);
        } catch (Exception ignore) {}
        return "unknown";
    }

    private String extractInResponseTo(String samlRequestB64) {
        if (samlRequestB64 == null) return null;
        try {
            String decoded = new String(Base64.getDecoder().decode(samlRequestB64), StandardCharsets.UTF_8);
            int idx = decoded.indexOf("ID=\"");
            if (idx >= 0) {
                int end = decoded.indexOf("\"", idx + 4);
                return decoded.substring(idx + 4, end);
            }
        } catch (Exception ignore) {}
        return null;
    }

    private String getBaseUrl(HttpServletRequest request) {
        return request.getScheme() + "://" + request.getServerName()
            + (request.getServerPort() != 80 && request.getServerPort() != 443
                ? ":" + request.getServerPort() : "");
    }
}
