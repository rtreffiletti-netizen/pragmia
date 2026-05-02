package io.pragmia.saml;

import org.springframework.context.annotation.*;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.saml2.provider.service.authentication.OpenSaml4AuthenticationProvider;
import org.springframework.security.saml2.provider.service.web.HttpSessionOpenSaml4AuthenticationRequestRepository;
import org.springframework.security.saml2.provider.service.web.authentication.HttpRedirectDeflateBindingDecoder;
import org.springframework.security.saml2.provider.service.web.authentication.HttpRedirectDeflateBindingEncoder;
import org.springframework.security.saml2.provider.service.web.authentication.Saml2AuthenticatedPrincipal;
import org.springframework.security.saml2.provider.service.web.authentication.Saml2WebSsoAuthenticationFilter;
import org.springframework.security.saml2.provider.service.web.authentication.Saml2WebSsoAuthenticationRequestRepository;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@Order(4)
public class SamlConfig {

    private static final String[] SAML_PATHS = {
        "/saml/idp/**", "/saml/sp/**"
    };

    @Bean
    public SecurityFilterChain samlFilterChain(HttpSecurity http) throws Exception {
        http.securityMatcher("/saml/**")
            .authorizeHttpRequests(a -> a.anyRequest().permitAll())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
            .csrf(c -> c.ignoringRequestMatchers("/saml/**"));

        if (System.getProperty("saml.sp.enabled", "false").equals("true")) {
            Saml2WebSsoAuthenticationRequestRepository requestRepository =
                new HttpSessionOpenSaml4AuthenticationRequestRepository();

            Saml2WebSsoAuthenticationFilter filter =
                new Saml2WebSsoAuthenticationFilter(
                    new OpenSaml4AuthenticationProvider(),
                    "/saml/sp/sso",
                    requestRepository
                );

            HttpRedirectDeflateBindingDecoder decoder = new HttpRedirectDeflateBindingDecoder();
            filter.setRequestResolver(decoder);

            HttpRedirectDeflateBindingEncoder encoder = new HttpRedirectDeflateBindingEncoder();
            filter.setAuthnRequestResolver(encoder);

            http.addFilterAt(filter, Saml2WebSsoAuthenticationFilter.DEFAULT_FILTER_POSITION);
        }

        return http.build();
    }
}
