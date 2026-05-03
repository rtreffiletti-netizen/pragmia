package io.pragmia.saml.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;

@Slf4j
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(name = "pragmia.modules.saml.enabled", havingValue = "true", matchIfMissing = true)
public class SamlSecurityConfig {

    private final SamlProperties samlProperties;

    /**
     * Security chain SAML — Order(10) per non interferire con la chain OAuth2 di VIRGILIO (Order 1).
     * Gestisce tutti i path /saml/** .
     */
    @Bean
    @Order(10)
    public SecurityFilterChain samlFilterChain(HttpSecurity http,
                                               RelyingPartyRegistrationRepository registrations) throws Exception {
        http
            .securityMatcher("/saml/**", "/api/saml/**")
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/saml/idp/metadata",
                    "/saml/sp/*/metadata",
                    "/saml/sp/*/acs",
                    "/saml/sp/*/slo"
                ).permitAll()
                .anyRequest().authenticated()
            )
            .saml2Login(saml2 -> saml2
                .loginProcessingUrl("/saml/sp/{registrationId}/acs")
                .defaultSuccessUrl("/saml/sp/post-login", true)
            )
            .saml2Logout(logout -> logout
                .logoutUrl("/saml/sp/{registrationId}/slo")
            )
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/saml/**")
            );

        log.info("[PRAGMIA-SAML] SecurityFilterChain SAML attivato — IdP entityId: {}",
            samlProperties.getIdp().getEntityId());
        return http.build();
    }
}
