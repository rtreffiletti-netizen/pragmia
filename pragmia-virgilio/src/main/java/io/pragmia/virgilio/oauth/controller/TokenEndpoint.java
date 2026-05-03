package io.pragmia.virgilio.oauth.controller;

import io.pragmia.virgilio.oauth.model.TokenRequest;
import io.pragmia.virgilio.oauth.model.TokenResponse;
import io.pragmia.virgilio.oauth.service.TokenService;
import io.pragmia.virgilio.oauth.service.ClientAuthenticationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * OAuth2 Token Endpoint
 * RFC 6749 - Section 3.2
 * Handles token requests for all grant types
 */
@Slf4j
@RestController
@RequestMapping("/oauth2")
@RequiredArgsConstructor
public class TokenEndpoint {

    private final TokenService tokenService;
    private final ClientAuthenticationService clientAuthenticationService;

    /**
     * OAuth2 Token Endpoint
     * POST /oauth2/token
     * 
     * Supported grant types:
     * - authorization_code
     * - refresh_token
     * - client_credentials
     * - password (if enabled)
     * - urn:ietf:params:oauth:grant-type:device_code
     * - urn:ietf:params:oauth:grant-type:token-exchange
     */
    @PostMapping(value = "/token", 
                 consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> token(
            @RequestParam Map<String, String> parameters,
            HttpServletRequest request) {
        
        try {
            log.info("Token request received - grant_type: {}", parameters.get("grant_type"));

            // Client authentication (client_id + client_secret via Basic Auth or form)
            String clientId = clientAuthenticationService.authenticateClient(request, parameters);
            if (clientId == null) {
                return errorResponse("invalid_client", "Client authentication failed");
            }

            // Build token request
            TokenRequest tokenRequest = TokenRequest.builder()
                    .grantType(parameters.get("grant_type"))
                    .clientId(clientId)
                    .code(parameters.get("code"))
                    .redirectUri(parameters.get("redirect_uri"))
                    .refreshToken(parameters.get("refresh_token"))
                    .scope(parameters.get("scope"))
                    .username(parameters.get("username"))
                    .password(parameters.get("password"))
                    .codeVerifier(parameters.get("code_verifier"))
                    .deviceCode(parameters.get("device_code"))
                    .subjectToken(parameters.get("subject_token"))
                    .subjectTokenType(parameters.get("subject_token_type"))
                    .actorToken(parameters.get("actor_token"))
                    .actorTokenType(parameters.get("actor_token_type"))
                    .resource(parameters.get("resource"))
                    .audience(parameters.get("audience"))
                    .build();

            // Process token request
            TokenResponse tokenResponse = tokenService.processTokenRequest(tokenRequest);

            log.info("Token issued successfully for client: {}", clientId);
            return ResponseEntity.ok(tokenResponse);

        } catch (IllegalArgumentException e) {
            log.warn("Invalid token request: {}", e.getMessage());
            return errorResponse("invalid_request", e.getMessage());
        } catch (SecurityException e) {
            log.warn("Unauthorized token request: {}", e.getMessage());
            return errorResponse("invalid_grant", e.getMessage());
        } catch (Exception e) {
            log.error("Error processing token request", e);
            return errorResponse("server_error", "Internal server error");
        }
    }

    /**
     * Token Revocation Endpoint
     * RFC 7009
     * POST /oauth2/revoke
     */
    @PostMapping(value = "/revoke",
                 consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<?> revoke(
            @RequestParam("token") String token,
            @RequestParam(value = "token_type_hint", required = false) String tokenTypeHint,
            HttpServletRequest request,
            @RequestParam Map<String, String> parameters) {
        
        try {
            // Client authentication required
            String clientId = clientAuthenticationService.authenticateClient(request, parameters);
            if (clientId == null) {
                return errorResponse("invalid_client", "Client authentication failed");
            }

            tokenService.revokeToken(token, tokenTypeHint, clientId);
            log.info("Token revoked for client: {}", clientId);
            
            return ResponseEntity.ok().build();

        } catch (Exception e) {
            log.error("Error revoking token", e);
            // RFC 7009: The authorization server responds with HTTP 200 even if invalid
            return ResponseEntity.ok().build();
        }
    }

    /**
     * Token Introspection Endpoint
     * RFC 7662
     * POST /oauth2/introspect
     */
    @PostMapping(value = "/introspect",
                 consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> introspect(
            @RequestParam("token") String token,
            @RequestParam(value = "token_type_hint", required = false) String tokenTypeHint,
            HttpServletRequest request,
            @RequestParam Map<String, String> parameters) {
        
        try {
            // Client authentication required
            String clientId = clientAuthenticationService.authenticateClient(request, parameters);
            if (clientId == null) {
                return errorResponse("invalid_client", "Client authentication failed");
            }

            Map<String, Object> introspectionResult = 
                tokenService.introspectToken(token, tokenTypeHint, clientId);
            
            return ResponseEntity.ok(introspectionResult);

        } catch (Exception e) {
            log.error("Error introspecting token", e);
            return ResponseEntity.ok(Map.of("active", false));
        }
    }

    /**
     * Device Authorization Endpoint
     * RFC 8628
     * POST /oauth2/device/authorize
     */
    @PostMapping(value = "/device/authorize",
                 consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> deviceAuthorize(
            @RequestParam("client_id") String clientId,
            @RequestParam(value = "scope", required = false) String scope) {
        
        try {
            Map<String, Object> deviceAuthResponse = 
                tokenService.initiateDeviceFlow(clientId, scope);
            
            return ResponseEntity.ok(deviceAuthResponse);

        } catch (IllegalArgumentException e) {
            return errorResponse("invalid_request", e.getMessage());
        } catch (Exception e) {
            log.error("Error initiating device flow", e);
            return errorResponse("server_error", "Internal server error");
        }
    }

    private ResponseEntity<?> errorResponse(String error, String description) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                    "error", error,
                    "error_description", description
                ));
    }
}
