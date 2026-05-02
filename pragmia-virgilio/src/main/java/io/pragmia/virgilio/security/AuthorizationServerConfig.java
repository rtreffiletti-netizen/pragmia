package io.pragmia.virgilio.security;

import com.nimbusds.jose.jwk.*;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jose.jwk.source.*;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.context.annotation.*;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.core.*;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.client.*;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.*;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.*;
import org.springframework.security.web.SecurityFilterChain;
import java.time.Duration;
import java.util.UUID;

@Configuration
public class AuthorizationServerConfig {

    @Bean @Order(1)
    public SecurityFilterChain authFilterChain(HttpSecurity http) throws Exception {
        OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);
        http.getConfigurer(OAuth2AuthorizationServerConfigurer.class).oidc(o -> {});
        return http.build();
    }

    @Bean
    public RegisteredClientRepository registeredClientRepository() {
        RegisteredClient client = RegisteredClient.withId(UUID.randomUUID().toString())
            .clientId("pragmia-admin-console")
            .clientAuthenticationMethod(ClientAuthenticationMethod.NONE)
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
            .redirectUri("http://localhost:8080/callback")
            .scope(OidcScopes.OPENID).scope(OidcScopes.PROFILE).scope("pragmia:admin")
            .clientSettings(ClientSettings.builder()
                .requireProofKey(true)
                .requireAuthorizationConsent(false)
                .build())
            .tokenSettings(TokenSettings.builder()
                .accessTokenTimeToLive(Duration.ofMinutes(15))
                .refreshTokenTimeToLive(Duration.ofHours(8))
                .reuseRefreshTokens(false)
                .build())
            .build();
        return new InMemoryRegisteredClientRepository(client);
    }

    @Bean
    public JWKSource<SecurityContext> jwkSource() {
        try {
            RSAKey rsaKey = new RSAKeyGenerator(2048)
                .keyID(UUID.randomUUID().toString()).generate();
            return new ImmutableJWKSet<>(new JWKSet(rsaKey));
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder()
            .issuer("http://localhost:8080")
            .authorizationEndpoint("/oauth2/authorize")
            .tokenEndpoint("/oauth2/token")
            .jwkSetEndpoint("/oauth2/jwks")
            .tokenRevocationEndpoint("/oauth2/revoke")
            .tokenIntrospectionEndpoint("/oauth2/introspect")
            .oidcUserInfoEndpoint("/userinfo")
            .build();
    }
}
