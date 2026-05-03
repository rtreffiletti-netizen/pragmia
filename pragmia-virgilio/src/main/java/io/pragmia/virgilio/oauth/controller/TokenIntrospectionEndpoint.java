
package io.pragmia.virgilio.oauth.controller;

import io.pragmia.virgilio.oauth.model.OAuthAccessToken;
import io.pragmia.virgilio.oauth.repository.OAuthAccessTokenRepository;
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

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Token Introspection Endpoint — RFC 7662
 * POST /oauth2/introspect
 *
 * Permette ai Resource Server di verificare la validità di un token
 * senza conoscere la chiave di firma JWT.
 */
@Slf4j
@RestController
@RequestMapping("/oauth2")
@RequiredArgsConstructor
@Tag(name = "OAuth2", description = "OAuth2 endpoints — RFC 6749, RFC 7662")
public class TokenIntrospectionEndpoint {

    private final OAuthAccessTokenRepository accessTokenRepository;
    private final TokenRevocationService revocationService;
    private final ClientAuthenticationService clientAuthenticationService;

    @Operation(summary = "Token Introspection — RFC 7662")
    @PostMapping(
        value = "/introspect",
        consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Map<String, Object>> introspect(
            @RequestParam Map<String, String> parameters,
            HttpServletRequest request) {

        // Autentica il client richiedente
        String clientId = clientAuthenticationService.authenticateClient(request, parameters);
        if (clientId == null) {
            return ResponseEntity.status(401).body(Map.of(
                "error", "invalid_client",
                "error_description", "Client authentication failed"
            ));
        }

        String tokenValue = parameters.get("token");
        if (tokenValue == null || tokenValue.isBlank()) {
            return ResponseEntity.ok(Map.of("active", false));
        }

        // Controlla prima la blacklist Redis (O(1))
        if (revocationService.isBlacklisted(tokenValue)) {
            log.debug("[INTROSPECT] Token in blacklist Redis");
            return ResponseEntity.ok(Map.of("active", false));
        }

        Optional<OAuthAccessToken> opt = accessTokenRepository.findByTokenValue(tokenValue);
        if (opt.isEmpty() || !opt.get().isValid()) {
            return ResponseEntity.ok(Map.of("active", false));
        }

        OAuthAccessToken token = opt.get();
        Map<String, Object> response = new HashMap<>();
        response.put("active",     true);
        response.put("sub",        token.getUserId().toString());
        response.put("client_id",  token.getClientId());
        response.put("scope",      token.getScope());
        response.put("token_type", token.getTokenType());
        response.put("iat",        token.getIssuedAt().getEpochSecond());
        response.put("exp",        token.getExpiresAt().getEpochSecond());
        if (token.getAudience()  != null) response.put("aud", token.getAudience());
        if (token.getIssuer()    != null) response.put("iss", token.getIssuer());

        log.debug("[INTROSPECT] Token attivo per userId={}", token.getUserId());
        return ResponseEntity.ok(response);
    }
}
