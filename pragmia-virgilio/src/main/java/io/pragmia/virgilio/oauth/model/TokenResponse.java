package io.pragmia.virgilio.oauth.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * OAuth2 Token Response
 * RFC 6749 - Section 5.1
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TokenResponse {
    
    @JsonProperty("access_token")
    private String accessToken;
    
    @JsonProperty("token_type")
    private String tokenType; // typically "Bearer"
    
    @JsonProperty("expires_in")
    private Long expiresIn; // seconds
    
    @JsonProperty("refresh_token")
    private String refreshToken;
    
    @JsonProperty("scope")
    private String scope;
    
    // OpenID Connect
    @JsonProperty("id_token")
    private String idToken;
    
    // Token Exchange (RFC 8693)
    @JsonProperty("issued_token_type")
    private String issuedTokenType;
    
    // Device Flow (RFC 8628)
    @JsonProperty("device_code")
    private String deviceCode;
    
    @JsonProperty("user_code")
    private String userCode;
    
    @JsonProperty("verification_uri")
    private String verificationUri;
    
    @JsonProperty("verification_uri_complete")
    private String verificationUriComplete;
    
    @JsonProperty("interval")
    private Integer interval; // polling interval in seconds
}
