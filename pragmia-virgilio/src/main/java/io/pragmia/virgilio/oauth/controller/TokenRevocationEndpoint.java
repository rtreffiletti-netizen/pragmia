
package io.pragmia.virgilio.oauth.controller;

import io.pragmia.virgilio.oauth.service.ClientAuthenticationService;
import io.pragmia.virgilio.oauth.service.TokenRevocationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Token Revocation Endpoint — RFC 7009
 * POST /oauth2/revoke
 *
 * Parametri form-urlencoded:
 *   token            — (obbligatorio) valore del token da revocare
 *   token_type_hint  — (opzionale) "access_token" | "refresh_token"
 *
 * Risponde sempre HTTP 200 OK per non rivelare l'esistenza del token (RFC 7009 §2.2).
 */
@Slf4j
@RestController
@RequestMapping("/oauth2")
@RequiredArgsConstructor
@Tag(name = "OAuth2", description = "OAuth2 endpoints — RFC 6749, RFC 7009")
public class TokenRevocationEndpoint {

    private final TokenRevocationService revocationService;
    private final ClientAuthenticationService clientAuthenticationService;

    @Operation(
        summary = "Token Revocation — RFC 7009",
        description = "Revoca un access token o refresh token. Risponde sempre 200 OK."
    )
    @PostMapping(
        value = "/revoke",
        consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> revoke(
            @RequestParam Map<String, String> parameters,
            HttpServletRequest request) {

        String token         = parameters.get("token");
        String tokenTypeHint = parameters.get("token_type_hint");

        if (token == null || token.isBlank()) {
            // RFC 7009 §2.2.1: invalid_request se manca il token
            return ResponseEntity.badRequest().body(
                Map.of("error", "invalid_request", "error_description", "Missing token parameter")
            );
        }

        // Autentica il client (Basic Auth o client_id/client_secret in form)
        String clientId = clientAuthenticationService.authenticateClient(request, parameters);
        if (clientId == null) {
            return ResponseEntity.status(401).body(
                Map.of("error", "invalid_client", "error_description", "Client authentication failed")
            );
        }

        log.info("[REVOCATION] Richiesta revoca token da clientId={} hint={}", clientId, tokenTypeHint);

        // RFC 7009: risponde sempre 200 OK, anche se il token non esiste
        revocationService.revokeToken(token, tokenTypeHint, clientId);

        return ResponseEntity.ok().build();
    }
}
