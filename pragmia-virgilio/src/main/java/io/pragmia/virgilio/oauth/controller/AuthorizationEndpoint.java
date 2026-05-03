package io.pragmia.virgilio.oauth.controller;

import io.pragmia.virgilio.oauth.model.OAuthAuthorizationCode;
import io.pragmia.virgilio.oauth.model.OAuthClient;
import io.pragmia.virgilio.oauth.repository.OAuthAuthorizationCodeRepository;
import io.pragmia.virgilio.oauth.repository.OAuthClientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

/**
 * OAuth2 Authorization Endpoint
 * RFC 6749 - Section 3.1
 * Handles authorization code flow
 */
@Slf4j
@Controller
@RequestMapping("/oauth2")
@RequiredArgsConstructor
public class AuthorizationEndpoint {

    private final OAuthClientRepository clientRepository;
    private final OAuthAuthorizationCodeRepository authCodeRepository;

    @Value("${virgilio.oauth.authorization-code.ttl:600}")
    private long authorizationCodeTtl;

    /**
     * Authorization Endpoint
     * GET /oauth2/authorize
     * 
     * Parameters:
     * - response_type: code (for authorization code flow)
     * - client_id: client identifier
     * - redirect_uri: callback URL
     * - scope: requested scopes
     * - state: client state for CSRF protection
     * - code_challenge: PKCE challenge (optional)
     * - code_challenge_method: S256 or plain (optional)
     */
    @GetMapping("/authorize")
    public RedirectView authorize(
            @RequestParam("response_type") String responseType,
            @RequestParam("client_id") String clientId,
            @RequestParam(value = "redirect_uri", required = false) String redirectUri,
            @RequestParam(value = "scope", required = false) String scope,
            @RequestParam(value = "state", required = false) String state,
            @RequestParam(value = "code_challenge", required = false) String codeChallenge,
            @RequestParam(value = "code_challenge_method", required = false, defaultValue = "plain") String codeChallengeMethod,
            Authentication authentication) {

        try {
            // Validate response_type
            if (!"code".equals(responseType)) {
                return errorRedirect(redirectUri, "unsupported_response_type", 
                                   "Only 'code' response_type is supported", state);
            }

            // Validate client
            OAuthClient client = clientRepository.findByClientId(clientId)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid client_id"));

            // Validate redirect_uri
            String validatedRedirectUri = validateRedirectUri(client, redirectUri);
            if (validatedRedirectUri == null) {
                return errorRedirect(null, "invalid_request", 
                                   "Invalid redirect_uri", state);
            }

            // Check user authentication
            if (authentication == null || !authentication.isAuthenticated()) {
                // Redirect to login with return URL
                String loginUrl = "/login?return_url=/oauth2/authorize" +
                                "?response_type=" + responseType +
                                "&client_id=" + clientId +
                                (redirectUri != null ? "&redirect_uri=" + redirectUri : "") +
                                (scope != null ? "&scope=" + scope : "") +
                                (state != null ? "&state=" + state : "") +
                                (codeChallenge != null ? "&code_challenge=" + codeChallenge : "") +
                                (codeChallengeMethod != null ? "&code_challenge_method=" + codeChallengeMethod : "");
                return new RedirectView(loginUrl);
            }

            // Generate authorization code
            String code = generateAuthorizationCode();
            String userId = authentication.getName(); // User ID from authenticated session

            // Store authorization code
            OAuthAuthorizationCode authCode = new OAuthAuthorizationCode();
            authCode.setCode(code);
            authCode.setClientId(clientId);
            authCode.setUserId(userId);
            authCode.setRedirectUri(validatedRedirectUri);
            authCode.setScope(scope);
            authCode.setCodeChallenge(codeChallenge);
            authCode.setCodeChallengeMethod(codeChallengeMethod);
            authCode.setIssuedAt(Instant.now());
            authCode.setExpiresAt(Instant.now().plusSeconds(authorizationCodeTtl));
            authCodeRepository.save(authCode);

            // Redirect to client with authorization code
            String redirectUrl = validatedRedirectUri +
                               (validatedRedirectUri.contains("?") ? "&" : "?") +
                               "code=" + code +
                               (state != null ? "&state=" + state : "");

            log.info("Authorization code issued for client: {} user: {}", clientId, userId);
            return new RedirectView(redirectUrl);

        } catch (IllegalArgumentException e) {
            log.warn("Authorization error: {}", e.getMessage());
            return errorRedirect(redirectUri, "invalid_request", e.getMessage(), state);
        } catch (Exception e) {
            log.error("Unexpected error during authorization", e);
            return errorRedirect(redirectUri, "server_error", "Internal server error", state);
        }
    }

    /**
     * Validate redirect_uri against client's registered URIs
     */
    private String validateRedirectUri(OAuthClient client, String requestedRedirectUri) {
        if (requestedRedirectUri == null || requestedRedirectUri.isEmpty()) {
            // Use default redirect URI if available
            return client.getRedirectUri();
        }

        // Check if requested URI matches registered URI
        if (requestedRedirectUri.equals(client.getRedirectUri())) {
            return requestedRedirectUri;
        }

        // Additional registered URIs could be stored as comma-separated
        String registeredUris = client.getRedirectUri();
        if (registeredUris != null) {
            for (String uri : registeredUris.split(",")) {
                if (requestedRedirectUri.equals(uri.trim())) {
                    return requestedRedirectUri;
                }
            }
        }

        log.warn("Redirect URI mismatch - requested: {}, registered: {}", 
                 requestedRedirectUri, client.getRedirectUri());
        return null;
    }

    /**
     * Generate secure authorization code
     */
    private String generateAuthorizationCode() {
        return UUID.randomUUID().toString().replace("-", "") +
               UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * Create error redirect
     */
    private RedirectView errorRedirect(String redirectUri, String error, 
                                      String errorDescription, String state) {
        if (redirectUri == null || redirectUri.isEmpty()) {
            // Cannot redirect, show error page
            return new RedirectView("/error?error=" + error + 
                                  "&error_description=" + errorDescription);
        }

        String errorUrl = redirectUri +
                         (redirectUri.contains("?") ? "&" : "?") +
                         "error=" + error +
                         "&error_description=" + errorDescription +
                         (state != null ? "&state=" + state : "");

        return new RedirectView(errorUrl);
    }
}
