package io.pragmia.virgilio;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.web.SecurityFilterChain;

@AutoConfiguration
@ConditionalOnProperty(prefix = "pragmia.oidc", name = "enabled", havingValue = "true")
public class OidcAutoConfiguration {

    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder().build();
    }

    @Bean
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
        OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);
        http.getConfigurer(OAuth2AuthorizationServerConfigurer.class).oidc(customizer -> {});
        http.exceptionHandling(ex -> ex.accessDeniedHandler(new OAuth2AuthorizationServerConfiguration.OAuth2AuthenticationEntryPoint()));
        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        String issuerUri = System.getProperty("oci.issuer-uri", "http://localhost:8080");
        String jwkSetUri = issuerUri + "/oauth2/jwks";
        return NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
    }
}
