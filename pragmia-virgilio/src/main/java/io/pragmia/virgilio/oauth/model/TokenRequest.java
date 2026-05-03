package io.pragmia.virgilio.oauth.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * OAuth2 Token Request
 * Encapsulates parameters for token endpoint requests
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenRequest {
    
    // Common parameters
    private String grantType;
    private String clientId;
    private String scope;
    
    // Authorization Code grant
    private String code;
    private String redirectUri;
    private String codeVerifier; // PKCE
    
    // Refresh Token grant
    private String refreshToken;
    
    // Resource Owner Password Credentials grant
    private String username;
    private String password;
    
    // Device Authorization grant (RFC 8628)
    private String deviceCode;
    
    // Token Exchange grant (RFC 8693)
    private String subjectToken;
    private String subjectTokenType;
    private String actorToken;
    private String actorTokenType;
    private String resource;
    private String audience;
}
