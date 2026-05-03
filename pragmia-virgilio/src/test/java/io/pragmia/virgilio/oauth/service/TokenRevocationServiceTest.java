
package io.pragmia.virgilio.oauth.service;

import io.pragmia.virgilio.oauth.model.OAuthAccessToken;
import io.pragmia.virgilio.oauth.model.OAuthRefreshToken;
import io.pragmia.virgilio.oauth.repository.OAuthAccessTokenRepository;
import io.pragmia.virgilio.oauth.repository.OAuthRefreshTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TokenRevocationServiceTest {

    @Mock OAuthAccessTokenRepository accessTokenRepo;
    @Mock OAuthRefreshTokenRepository refreshTokenRepo;
    @Mock StringRedisTemplate redisTemplate;
    @Mock ValueOperations<String, String> valueOps;
    @InjectMocks TokenRevocationService service;

    private OAuthAccessToken validAccessToken;
    private OAuthRefreshToken validRefreshToken;

    @BeforeEach
    void setUp() {
        UUID userId = UUID.randomUUID();
        validAccessToken = OAuthAccessToken.builder()
            .id(UUID.randomUUID()).tokenValue("access-token-xyz")
            .userId(userId).clientId("my-client")
            .issuedAt(Instant.now()).expiresAt(Instant.now().plusSeconds(900))
            .revoked(false).build();

        validRefreshToken = OAuthRefreshToken.builder()
            .id(UUID.randomUUID()).tokenValue("refresh-token-xyz")
            .userId(userId).clientId("my-client")
            .issuedAt(Instant.now()).expiresAt(Instant.now().plusSeconds(86400))
            .accessTokenId(validAccessToken.getId()).revoked(false).build();

        when(redisTemplate.opsForValue()).thenReturn(valueOps);
    }

    @Test
    void revokeToken_accessToken_shouldMarkRevokedAndBlacklist() {
        when(accessTokenRepo.findByTokenValue("access-token-xyz")).thenReturn(Optional.of(validAccessToken));

        service.revokeToken("access-token-xyz", "access_token", "my-client");

        assertThat(validAccessToken.isRevoked()).isTrue();
        assertThat(validAccessToken.getRevokedAt()).isNotNull();
        verify(accessTokenRepo).save(validAccessToken);
        verify(valueOps).set(contains("access-token-xyz"), eq("revoked"), any());
    }

    @Test
    void revokeToken_refreshToken_shouldRevokeRefreshAndLinkedAccess() {
        when(refreshTokenRepo.findByTokenValue("refresh-token-xyz")).thenReturn(Optional.of(validRefreshToken));
        when(accessTokenRepo.findById(validRefreshToken.getAccessTokenId())).thenReturn(Optional.of(validAccessToken));

        service.revokeToken("refresh-token-xyz", "refresh_token", "my-client");

        assertThat(validRefreshToken.isRevoked()).isTrue();
        assertThat(validAccessToken.isRevoked()).isTrue();
        verify(refreshTokenRepo).save(validRefreshToken);
        verify(accessTokenRepo).save(validAccessToken);
    }

    @Test
    void revokeToken_clientMismatch_shouldNotRevoke() {
        when(accessTokenRepo.findByTokenValue("access-token-xyz")).thenReturn(Optional.of(validAccessToken));

        service.revokeToken("access-token-xyz", "access_token", "wrong-client");

        assertThat(validAccessToken.isRevoked()).isFalse();
        verify(accessTokenRepo, never()).save(any());
    }

    @Test
    void isBlacklisted_shouldReturnTrueWhenKeyExistsInRedis() {
        when(redisTemplate.hasKey(contains("some-token"))).thenReturn(Boolean.TRUE);
        assertThat(service.isBlacklisted("some-token")).isTrue();
    }

    @Test
    void isBlacklisted_shouldReturnFalseWhenKeyMissing() {
        when(redisTemplate.hasKey(any())).thenReturn(Boolean.FALSE);
        assertThat(service.isBlacklisted("some-token")).isFalse();
    }

    @Test
    void revokeAllUserTokensAllClients_shouldRevokeEverything() {
        UUID userId = validAccessToken.getUserId();
        when(accessTokenRepo.findByUserId(userId)).thenReturn(java.util.List.of(validAccessToken));
        when(refreshTokenRepo.findByUserId(userId)).thenReturn(java.util.List.of(validRefreshToken));

        service.revokeAllUserTokensAllClients(userId);

        assertThat(validAccessToken.isRevoked()).isTrue();
        assertThat(validRefreshToken.isRevoked()).isTrue();
    }
}
