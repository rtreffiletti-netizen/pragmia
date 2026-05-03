package io.pragmia.virgilio.oauth.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * OpenID Connect Discovery Endpoint
 * OpenID Connect Discovery 1.0
 * Provides metadata about the OpenID Provider
 */
@RestController
@RequiredArgsConstructor
public class OidcDiscoveryEndpoint {

    @Value("${virgilio.issuer:https://auth.pragmia.io}")
    private String issuer;

    /**
     * OpenID Connect Discovery
     * GET /.well-known/openid-configuration
     */
    @GetMapping(value = "/.well-known/openid-configuration", 
                produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> discover() {
        
        Map<String, Object> metadata = Map.ofEntries(
                // Issuer identifier
                Map.entry("issuer", issuer),
                
                // Authorization endpoint
                Map.entry("authorization_endpoint", issuer + "/oauth2/authorize"),
                
                // Token endpoint
                Map.entry("token_endpoint", issuer + "/oauth2/token"),
                
                // UserInfo endpoint
                Map.entry("userinfo_endpoint", issuer + "/oauth2/userinfo"),
                
                // JWK Set endpoint
                Map.entry("jwks_uri", issuer + "/oauth2/jwks"),
                
                // Token revocation endpoint (RFC 7009)
                Map.entry("revocation_endpoint", issuer + "/oauth2/revoke"),
                
                // Token introspection endpoint (RFC 7662)
                Map.entry("introspection_endpoint", issuer + "/oauth2/introspect"),
                
                // Device authorization endpoint (RFC 8628)
                Map.entry("device_authorization_endpoint", issuer + "/oauth2/device/authorize"),
                
                // Supported response types
                Map.entry("response_types_supported", List.of(
                    "code",
                    "token",
                    "id_token",
                    "code token",
                    "code id_token",
                    "token id_token",
                    "code token id_token"
                )),
                
                // Supported grant types
                Map.entry("grant_types_supported", List.of(
                    "authorization_code",
                    "implicit",
                    "password",
                    "client_credentials",
                    "refresh_token",
                    "urn:ietf:params:oauth:grant-type:device_code"
                )),
                
                // Supported scopes
                Map.entry("scopes_supported", List.of(
                    "openid",
                    "profile",
                    "email",
                    "address",
                    "phone",
                    "offline_access"
                )),
                
                // Supported subject types
                Map.entry("subject_types_supported", List.of("public")),
                
                // Supported ID token signing algorithms
                Map.entry("id_token_signing_alg_values_supported", List.of(
                    "RS256",
                    "RS384",
                    "RS512",
                    "ES256",
                    "ES384",
                    "ES512"
                )),
                
                // Supported claims
                Map.entry("claims_supported", List.of(
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
                )),
                
                // Token endpoint authentication methods
                Map.entry("token_endpoint_auth_methods_supported", List.of(
                    "client_secret_basic",
                    "client_secret_post",
                    "client_secret_jwt",
                    "private_key_jwt"
                )),
                
                // PKCE support
                Map.entry("code_challenge_methods_supported", List.of(
                    "plain",
                    "S256"
                )),
                
                // Request parameter support
                Map.entry("request_parameter_supported", true),
                Map.entry("request_uri_parameter_supported", false),
                
                // Claims parameter support
                Map.entry("claims_parameter_supported", true),
                
                // ACR values supported
                Map.entry("acr_values_supported", List.of(
                    "urn:pragmia:bronze",
                    "urn:pragmia:silver",
                    "urn:pragmia:gold"
                ))
        );

        return ResponseEntity.ok(metadata);
    }

    /**
     * OAuth 2.0 Authorization Server Metadata
     * RFC 8414
     * GET /.well-known/oauth-authorization-server
     */
    @GetMapping(value = "/.well-known/oauth-authorization-server",
                produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> oauthMetadata() {
        // OAuth 2.0 metadata (subset of OIDC metadata)
        Map<String, Object> metadata = Map.ofEntries(
                Map.entry("issuer", issuer),
                Map.entry("authorization_endpoint", issuer + "/oauth2/authorize"),
                Map.entry("token_endpoint", issuer + "/oauth2/token"),
                Map.entry("jwks_uri", issuer + "/oauth2/jwks"),
                Map.entry("revocation_endpoint", issuer + "/oauth2/revoke"),
                Map.entry("introspection_endpoint", issuer + "/oauth2/introspect"),
                Map.entry("device_authorization_endpoint", issuer + "/oauth2/device/authorize"),
                Map.entry("response_types_supported", List.of("code", "token")),
                Map.entry("grant_types_supported", List.of(
                    "authorization_code",
                    "client_credentials",
                    "refresh_token",
                    "urn:ietf:params:oauth:grant-type:device_code"
                )),
                Map.entry("token_endpoint_auth_methods_supported", List.of(
                    "client_secret_basic",
                    "client_secret_post"
                )),
                Map.entry("code_challenge_methods_supported", List.of("plain", "S256"))
        );

        return ResponseEntity.ok(metadata);
    }
}
