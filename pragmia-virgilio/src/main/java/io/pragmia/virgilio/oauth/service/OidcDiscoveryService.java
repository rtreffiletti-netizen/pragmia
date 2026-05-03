package io.pragmia.virgilio.oauth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class OidcDiscoveryService {

    @Value("${pragmia.oauth.issuer}")
    private String issuer;

    @Value("${pragmia.oauth.authorization-endpoint}")
    private String authorizationEndpoint;

    @Value("${pragmia.oauth.token-endpoint}")
    private String tokenEndpoint;

    @Value("${pragmia.oauth.userinfo-endpoint}")
    private String userinfoEndpoint;

    @Value("${pragmia.oauth.jwks-uri}")
    private String jwksUri;

    @Value("${pragmia.oauth.registration-endpoint:}")
    private String registrationEndpoint;

    @Value("${pragmia.oauth.revocation-endpoint:}")
    private String revocationEndpoint;

    @Value("${pragmia.oauth.introspection-endpoint:}")
    private String introspectionEndpoint;

    /**
     * Returns OpenID Connect Discovery metadata
     */
    public Map<String, Object> getDiscoveryMetadata() {
        Map<String, Object> metadata = new LinkedHashMap<>();
        
        // Required fields
        metadata.put("issuer", issuer);
        metadata.put("authorization_endpoint", authorizationEndpoint);
        metadata.put("token_endpoint", tokenEndpoint);
        metadata.put("userinfo_endpoint", userinfoEndpoint);
        metadata.put("jwks_uri", jwksUri);
        
        // Response types supported
        metadata.put("response_types_supported", Arrays.asList(
                "code",
                "token",
                "id_token",
                "code token",
                "code id_token",
                "token id_token",
                "code token id_token"
        ));
        
        // Subject types supported
        metadata.put("subject_types_supported", Arrays.asList("public"));
        
        // ID Token signing algorithms
        metadata.put("id_token_signing_alg_values_supported", Arrays.asList(
                "RS256",
                "ES256",
                "HS256"
        ));
        
        // Scopes supported
        metadata.put("scopes_supported", Arrays.asList(
                "openid",
                "profile",
                "email",
                "address",
                "phone",
                "offline_access"
        ));
        
        // Token endpoint auth methods
        metadata.put("token_endpoint_auth_methods_supported", Arrays.asList(
                "client_secret_basic",
                "client_secret_post",
                "client_secret_jwt",
                "private_key_jwt",
                "none"
        ));
        
        // Claims supported
        metadata.put("claims_supported", Arrays.asList(
                "sub",
                "iss",
                "aud",
                "exp",
                "iat",
                "auth_time",
                "nonce",
                "acr",
                "amr",
                "azp",
                "name",
                "given_name",
                "family_name",
                "middle_name",
                "nickname",
                "preferred_username",
                "profile",
                "picture",
                "website",
                "email",
                "email_verified",
                "gender",
                "birthdate",
                "zoneinfo",
                "locale",
                "phone_number",
                "phone_number_verified",
                "address",
                "updated_at"
        ));
        
        // Grant types supported
        metadata.put("grant_types_supported", Arrays.asList(
                "authorization_code",
                "refresh_token",
                "client_credentials",
                "password"
        ));
        
        // Response modes supported
        metadata.put("response_modes_supported", Arrays.asList(
                "query",
                "fragment",
                "form_post"
        ));
        
        // Code challenge methods (PKCE)
        metadata.put("code_challenge_methods_supported", Arrays.asList(
                "plain",
                "S256"
        ));
        
        // Token endpoint auth signing algorithms
        metadata.put("token_endpoint_auth_signing_alg_values_supported", Arrays.asList(
                "RS256",
                "ES256",
                "HS256"
        ));
        
        // Display values supported
        metadata.put("display_values_supported", Arrays.asList(
                "page",
                "popup"
        ));
        
        // Claim types supported
        metadata.put("claim_types_supported", Arrays.asList("normal"));
        
        // Optional endpoints
        if (registrationEndpoint != null && !registrationEndpoint.isEmpty()) {
            metadata.put("registration_endpoint", registrationEndpoint);
        }
        if (revocationEndpoint != null && !revocationEndpoint.isEmpty()) {
            metadata.put("revocation_endpoint", revocationEndpoint);
        }
        if (introspectionEndpoint != null && !introspectionEndpoint.isEmpty()) {
            metadata.put("introspection_endpoint", introspectionEndpoint);
        }
        
        // Additional OIDC features
        metadata.put("claims_parameter_supported", true);
        metadata.put("request_parameter_supported", true);
        metadata.put("request_uri_parameter_supported", false);
        metadata.put("require_request_uri_registration", false);
        
        // ACR values
        metadata.put("acr_values_supported", Arrays.asList(
                "urn:mace:incommon:iap:silver",
                "urn:mace:incommon:iap:bronze"
        ));
        
        log.info("Discovery metadata retrieved");
        return metadata;
    }

    /**
     * Returns JWKS (JSON Web Key Set) for token verification
     */
    public Map<String, Object> getJwks() {
        Map<String, Object> jwks = new LinkedHashMap<>();
        List<Map<String, Object>> keys = new ArrayList<>();
        
        // This is a placeholder - in production, load actual keys from keystore
        // For now, return empty key set
        jwks.put("keys", keys);
        
        log.info("JWKS retrieved");
        return jwks;
    }

    /**
     * Validates issuer configuration
     */
    public boolean validateIssuerConfiguration() {
        if (issuer == null || issuer.isEmpty()) {
            log.error("Issuer not configured");
            return false;
        }
        
        if (authorizationEndpoint == null || authorizationEndpoint.isEmpty()) {
            log.error("Authorization endpoint not configured");
            return false;
        }
        
        if (tokenEndpoint == null || tokenEndpoint.isEmpty()) {
            log.error("Token endpoint not configured");
            return false;
        }
        
        if (userinfoEndpoint == null || userinfoEndpoint.isEmpty()) {
            log.error("UserInfo endpoint not configured");
            return false;
        }
        
        if (jwksUri == null || jwksUri.isEmpty()) {
            log.error("JWKS URI not configured");
            return false;
        }
        
        log.info("Issuer configuration validated successfully");
        return true;
    }

    /**
     * Gets supported scopes
     */
    public List<String> getSupportedScopes() {
        return Arrays.asList(
                "openid",
                "profile",
                "email",
                "address",
                "phone",
                "offline_access"
        );
    }

    /**
     * Gets supported response types
     */
    public List<String> getSupportedResponseTypes() {
        return Arrays.asList(
                "code",
                "token",
                "id_token",
                "code token",
                "code id_token",
                "token id_token",
                "code token id_token"
        );
    }

    /**
     * Gets supported grant types
     */
    public List<String> getSupportedGrantTypes() {
        return Arrays.asList(
                "authorization_code",
                "refresh_token",
                "client_credentials",
                "password"
        );
    }
}
