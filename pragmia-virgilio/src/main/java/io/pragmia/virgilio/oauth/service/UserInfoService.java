package io.pragmia.virgilio.oauth.service;

import io.pragmia.virgilio.access.model.User;
import io.pragmia.virgilio.access.repository.UserRepository;
import io.pragmia.virgilio.oauth.model.OAuthAccessToken;
import io.pragmia.virgilio.oauth.repository.OAuthAccessTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserInfoService {

    private final OAuthAccessTokenRepository accessTokenRepository;
    private final UserRepository userRepository;

    /**
     * Retrieves user information based on access token
     */
    public Map<String, Object> getUserInfo(String accessToken) {
        // Validate and retrieve access token
        OAuthAccessToken token = validateAccessToken(accessToken);
        
        // Retrieve user
        User user = userRepository.findById(Long.parseLong(token.getUserId()))
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        // Build user info response based on scopes
        return buildUserInfoResponse(user, token.getScope());
    }

    /**
     * Validates the access token
     */
    private OAuthAccessToken validateAccessToken(String tokenValue) {
        OAuthAccessToken token = accessTokenRepository.findByAccessToken(tokenValue)
                .orElseThrow(() -> new IllegalArgumentException("Invalid access token"));
        
        // Check if token is expired
        if (token.getExpiresAt().isBefore(Instant.now())) {
            log.warn("Access token expired: {}", tokenValue);
            throw new IllegalArgumentException("Access token expired");
        }
        
        // Check if token is revoked
        if (token.isRevoked()) {
            log.warn("Access token revoked: {}", tokenValue);
            throw new IllegalArgumentException("Access token revoked");
        }
        
        return token;
    }

    /**
     * Builds user info response based on granted scopes
     */
    private Map<String, Object> buildUserInfoResponse(User user, String scope) {
        Map<String, Object> userInfo = new HashMap<>();
        
        Set<String> scopes = scope != null ? 
                new HashSet<>(Arrays.asList(scope.split(" "))) : 
                Collections.emptySet();
        
        // Subject (sub) is always included
        userInfo.put("sub", user.getId().toString());
        
        // Profile scope
        if (scopes.contains("profile")) {
            addProfileClaims(userInfo, user);
        }
        
        // Email scope
        if (scopes.contains("email")) {
            addEmailClaims(userInfo, user);
        }
        
        // Phone scope
        if (scopes.contains("phone")) {
            addPhoneClaims(userInfo, user);
        }
        
        // Address scope
        if (scopes.contains("address")) {
            addAddressClaims(userInfo, user);
        }
        
        log.info("User info retrieved for user: {}", user.getId());
        return userInfo;
    }

    /**
     * Adds profile claims to user info
     */
    private void addProfileClaims(Map<String, Object> userInfo, User user) {
        if (user.getUsername() != null) {
            userInfo.put("preferred_username", user.getUsername());
        }
        if (user.getFirstName() != null) {
            userInfo.put("given_name", user.getFirstName());
        }
        if (user.getLastName() != null) {
            userInfo.put("family_name", user.getLastName());
        }
        if (user.getFirstName() != null || user.getLastName() != null) {
            String fullName = String.format("%s %s", 
                    user.getFirstName() != null ? user.getFirstName() : "",
                    user.getLastName() != null ? user.getLastName() : ""
            ).trim();
            if (!fullName.isEmpty()) {
                userInfo.put("name", fullName);
            }
        }
        if (user.getPictureUrl() != null) {
            userInfo.put("picture", user.getPictureUrl());
        }
        if (user.getLocale() != null) {
            userInfo.put("locale", user.getLocale());
        }
        if (user.getTimezone() != null) {
            userInfo.put("zoneinfo", user.getTimezone());
        }
        if (user.getCreatedAt() != null) {
            userInfo.put("updated_at", user.getCreatedAt().getEpochSecond());
        }
    }

    /**
     * Adds email claims to user info
     */
    private void addEmailClaims(Map<String, Object> userInfo, User user) {
        if (user.getEmail() != null) {
            userInfo.put("email", user.getEmail());
            userInfo.put("email_verified", user.isEmailVerified());
        }
    }

    /**
     * Adds phone claims to user info
     */
    private void addPhoneClaims(Map<String, Object> userInfo, User user) {
        if (user.getPhoneNumber() != null) {
            userInfo.put("phone_number", user.getPhoneNumber());
            userInfo.put("phone_number_verified", user.isPhoneVerified());
        }
    }

    /**
     * Adds address claims to user info
     */
    private void addAddressClaims(Map<String, Object> userInfo, User user) {
        Map<String, String> address = new HashMap<>();
        boolean hasAddress = false;
        
        if (user.getStreetAddress() != null) {
            address.put("street_address", user.getStreetAddress());
            hasAddress = true;
        }
        if (user.getCity() != null) {
            address.put("locality", user.getCity());
            hasAddress = true;
        }
        if (user.getState() != null) {
            address.put("region", user.getState());
            hasAddress = true;
        }
        if (user.getPostalCode() != null) {
            address.put("postal_code", user.getPostalCode());
            hasAddress = true;
        }
        if (user.getCountry() != null) {
            address.put("country", user.getCountry());
            hasAddress = true;
        }
        
        if (hasAddress) {
            userInfo.put("address", address);
        }
    }

    /**
     * Gets user claims for ID token generation
     */
    public Map<String, Object> getUserClaims(String userId, String scope) {
        User user = userRepository.findById(Long.parseLong(userId))
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        return buildUserInfoResponse(user, scope);
    }

    /**
     * Validates if user has required claims
     */
    public boolean validateUserClaims(String userId, Set<String> requiredClaims) {
        try {
            User user = userRepository.findById(Long.parseLong(userId))
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            
            for (String claim : requiredClaims) {
                if (!hasClaimValue(user, claim)) {
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            log.error("Error validating user claims", e);
            return false;
        }
    }

    /**
     * Checks if user has value for specific claim
     */
    private boolean hasClaimValue(User user, String claim) {
        switch (claim) {
            case "email":
                return user.getEmail() != null;
            case "phone_number":
                return user.getPhoneNumber() != null;
            case "preferred_username":
                return user.getUsername() != null;
            case "given_name":
                return user.getFirstName() != null;
            case "family_name":
                return user.getLastName() != null;
            default:
                return false;
        }
    }
}
