package io.pragmia.virgilio.oauth.service;

import io.pragmia.virgilio.oauth.model.OAuthClient;
import io.pragmia.virgilio.oauth.repository.OAuthClientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

/**
 * Client Authentication Service
 * Handles OAuth2 client authentication
 * RFC 6749 - Section 2.3
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ClientAuthenticationService {

    private final OAuthClientRepository clientRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Authenticate OAuth2 client
     * Supports:
     * - HTTP Basic Authentication (Authorization header)
     * - Form-based authentication (client_id and client_secret in body)
     * 
     * @param request HTTP request
     * @param parameters Request parameters
     * @return clientId if authentication successful, null otherwise
     */
    public String authenticateClient(HttpServletRequest request, Map<String, String> parameters) {
        // Try Basic Authentication first
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Basic ")) {
            return authenticateClientBasic(authHeader);
        }

        // Try form-based authentication
        String clientId = parameters.get("client_id");
        String clientSecret = parameters.get("client_secret");
        
        if (clientId != null) {
            return authenticateClientForm(clientId, clientSecret);
        }

        log.warn("No client credentials provided");
        return null;
    }

    /**
     * Authenticate via HTTP Basic Authentication
     * RFC 2617
     */
    private String authenticateClientBasic(String authHeader) {
        try {
            String base64Credentials = authHeader.substring("Basic ".length());
            byte[] decodedBytes = Base64.getDecoder().decode(base64Credentials);
            String credentials = new String(decodedBytes, StandardCharsets.UTF_8);
            
            String[] parts = credentials.split(":", 2);
            if (parts.length != 2) {
                log.warn("Invalid Basic authentication format");
                return null;
            }

            String clientId = parts[0];
            String clientSecret = parts[1];

            return validateClientCredentials(clientId, clientSecret);

        } catch (Exception e) {
            log.error("Error parsing Basic authentication", e);
            return null;
        }
    }

    /**
     * Authenticate via form parameters
     */
    private String authenticateClientForm(String clientId, String clientSecret) {
        return validateClientCredentials(clientId, clientSecret);
    }

    /**
     * Validate client credentials against database
     */
    private String validateClientCredentials(String clientId, String clientSecret) {
        OAuthClient client = clientRepository.findByClientId(clientId)
                .orElse(null);

        if (client == null) {
            log.warn("Client not found: {}", clientId);
            return null;
        }

        // Public clients (no secret required for some grant types)
        if (client.getClientSecret() == null || client.getClientSecret().isEmpty()) {
            log.info("Public client authenticated: {}", clientId);
            return clientId;
        }

        // Confidential clients (secret required)
        if (clientSecret == null) {
            log.warn("Client secret required for confidential client: {}", clientId);
            return null;
        }

        if (!passwordEncoder.matches(clientSecret, client.getClientSecret())) {
            log.warn("Invalid client secret for client: {}", clientId);
            return null;
        }

        log.info("Client authenticated successfully: {}", clientId);
        return clientId;
    }
}
