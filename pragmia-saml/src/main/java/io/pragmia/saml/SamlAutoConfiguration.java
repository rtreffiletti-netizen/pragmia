package io.pragmia.saml;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistration;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrationRepository;

@AutoConfiguration
@ConditionalOnProperty(prefix = "pragmia.modules", name = "samlEnabled", havingValue = "true", matchIfMissing = false)
@EnableConfigurationProperties(SamlProperties.class)
@ComponentScan(basePackages = "io.pragmia.saml")
@EnableWebSecurity
public class SamlAutoConfiguration {

    @Bean
    public RelyingPartyRegistrationRepository relyingPartyRegistrationRepository() {
        return transactions -> {}; // custom provider-based
    }
}
