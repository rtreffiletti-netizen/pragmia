package io.pragmia.virgilio.security.jwt;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.*;
import com.nimbusds.jose.jwk.*;
import com.nimbusds.jwt.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.security.*;
import java.security.interfaces.*;
import java.text.ParseException;
import java.time.Instant;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtService {

    @Value("${pragmia.jwt.issuer}")
    private String issuer;

    @Value("${pragmia.jwt.access-token-expiration:3600}") // 1 hour default
    private long accessTokenExpiration;

    @Value("${pragmia.jwt.id-token-expiration:3600}") // 1 hour default
    private long idTokenExpiration;

    @Value("${pragmia.jwt.algorithm:RS256}")
    private String algorithm;

    private RSAKey rsaKey;
    private JWSSigner signer;
    private JWSVerifier verifier;

    @PostConstruct
    public void init() throws Exception {
        // Generate RSA key pair for signing (in production, load from keystore)
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        
        // Create RSA JWK
        rsaKey = new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID(UUID.randomUUID().toString())
                .algorithm(JWSAlgorithm.RS256)
                .build();
        
        // Create signer and verifier
        signer = new RSASSASigner(rsaKey);
        verifier = new RSASSAVerifier(rsaKey);
        
        log.info("JWT Service initialized with RSA256 signing");
    }

    /**
     * Generates an access token (JWT)
     */
    public String generateAccessToken(
            String userId,
            String clientId,
            String scope,
            Map<String, Object> additionalClaims) {
        
        try {
            Instant now = Instant.now();
            Instant expiration = now.plusSeconds(accessTokenExpiration);
            
            JWTClaimsSet.Builder claimsBuilder = new JWTClaimsSet.Builder()
                    .issuer(issuer)
                    .subject(userId)
                    .audience(clientId)
                    .expirationTime(Date.from(expiration))
                    .issueTime(Date.from(now))
                    .jwtID(UUID.randomUUID().toString())
                    .claim("scope", scope)
                    .claim("token_type", "Bearer");
            
            // Add additional claims
            if (additionalClaims != null) {
                additionalClaims.forEach(claimsBuilder::claim);
            }
            
            JWTClaimsSet claimsSet = claimsBuilder.build();
            
            // Create JWT
            SignedJWT signedJWT = new SignedJWT(
                    new JWSHeader.Builder(JWSAlgorithm.RS256)
                            .keyID(rsaKey.getKeyID())
                            .type(JOSEObjectType.JWT)
                            .build(),
                    claimsSet
            );
            
            // Sign the JWT
            signedJWT.sign(signer);
            
            log.info("Access token generated for user: {} and client: {}", userId, clientId);
            return signedJWT.serialize();
            
        } catch (Exception e) {
            log.error("Error generating access token", e);
            throw new RuntimeException("Failed to generate access token", e);
        }
    }

    /**
     * Generates an ID token (OIDC)
     */
    public String generateIdToken(
            String userId,
            String clientId,
            String nonce,
            Map<String, Object> userClaims) {
        
        try {
            Instant now = Instant.now();
            Instant expiration = now.plusSeconds(idTokenExpiration);
            
            JWTClaimsSet.Builder claimsBuilder = new JWTClaimsSet.Builder()
                    .issuer(issuer)
                    .subject(userId)
                    .audience(clientId)
                    .expirationTime(Date.from(expiration))
                    .issueTime(Date.from(now))
                    .claim("auth_time", now.getEpochSecond());
            
            // Add nonce if present (for preventing replay attacks)
            if (nonce != null && !nonce.isEmpty()) {
                claimsBuilder.claim("nonce", nonce);
            }
            
            // Add user claims (profile, email, etc.)
            if (userClaims != null) {
                userClaims.forEach(claimsBuilder::claim);
            }
            
            JWTClaimsSet claimsSet = claimsBuilder.build();
            
            // Create JWT
            SignedJWT signedJWT = new SignedJWT(
                    new JWSHeader.Builder(JWSAlgorithm.RS256)
                            .keyID(rsaKey.getKeyID())
                            .type(JOSEObjectType.JWT)
                            .build(),
                    claimsSet
            );
            
            // Sign the JWT
            signedJWT.sign(signer);
            
            log.info("ID token generated for user: {} and client: {}", userId, clientId);
            return signedJWT.serialize();
            
        } catch (Exception e) {
            log.error("Error generating ID token", e);
            throw new RuntimeException("Failed to generate ID token", e);
        }
    }

    /**
     * Validates and parses a JWT token
     */
    public JWTClaimsSet validateToken(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            
            // Verify signature
            if (!signedJWT.verify(verifier)) {
                throw new IllegalArgumentException("Invalid token signature");
            }
            
            JWTClaimsSet claims = signedJWT.getJWTClaimsSet();
            
            // Validate expiration
            Date expirationTime = claims.getExpirationTime();
            if (expirationTime != null && expirationTime.before(new Date())) {
                throw new IllegalArgumentException("Token expired");
            }
            
            // Validate issuer
            if (!issuer.equals(claims.getIssuer())) {
                throw new IllegalArgumentException("Invalid token issuer");
            }
            
            log.info("Token validated successfully for subject: {}", claims.getSubject());
            return claims;
            
        } catch (ParseException e) {
            log.error("Error parsing token", e);
            throw new IllegalArgumentException("Invalid token format", e);
        } catch (JOSEException e) {
            log.error("Error verifying token signature", e);
            throw new IllegalArgumentException("Token verification failed", e);
        }
    }

    /**
     * Extracts claims from token without full validation (for introspection)
     */
    public JWTClaimsSet parseTokenUnsafe(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            return signedJWT.getJWTClaimsSet();
        } catch (ParseException e) {
            log.error("Error parsing token", e);
            throw new IllegalArgumentException("Invalid token format", e);
        }
    }

    /**
     * Gets user ID from token
     */
    public String getUserIdFromToken(String token) {
        JWTClaimsSet claims = validateToken(token);
        return claims.getSubject();
    }

    /**
     * Gets scope from token
     */
    public String getScopeFromToken(String token) {
        JWTClaimsSet claims = validateToken(token);
        try {
            return claims.getStringClaim("scope");
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Checks if token is expired
     */
    public boolean isTokenExpired(String token) {
        try {
            JWTClaimsSet claims = parseTokenUnsafe(token);
            Date expirationTime = claims.getExpirationTime();
            return expirationTime != null && expirationTime.before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Gets token expiration time
     */
    public Instant getTokenExpiration(String token) {
        JWTClaimsSet claims = parseTokenUnsafe(token);
        Date expirationTime = claims.getExpirationTime();
        return expirationTime != null ? expirationTime.toInstant() : null;
    }

    /**
     * Returns public JWK for token verification (for JWKS endpoint)
     */
    public Map<String, Object> getPublicJwk() {
        try {
            RSAKey publicJWK = rsaKey.toPublicJWK();
            return publicJWK.toJSONObject();
        } catch (Exception e) {
            log.error("Error converting JWK to JSON", e);
            throw new RuntimeException("Failed to get public JWK", e);
        }
    }

    /**
     * Returns JWKS (JSON Web Key Set) for discovery endpoint
     */
    public Map<String, Object> getJwks() {
        Map<String, Object> jwks = new LinkedHashMap<>();
        List<Map<String, Object>> keys = new ArrayList<>();
        keys.add(getPublicJwk());
        jwks.put("keys", keys);
        return jwks;
    }

    /**
     * Gets JWT algorithm
     */
    public String getAlgorithm() {
        return algorithm;
    }

    /**
     * Gets token kid (key ID)
     */
    public String getKeyId() {
        return rsaKey.getKeyID();
    }
}
