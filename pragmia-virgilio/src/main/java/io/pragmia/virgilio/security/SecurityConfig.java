package io.pragmia.virgilio.security;

import org.springframework.context.annotation.*;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private static final String[] PUBLIC_PATHS = {
        "/login", "/login/**", "/oauth2/**", "/.well-known/**",
        "/actuator/health", "/swagger-ui/**", "/api-docs/**",
        "/error", "/static/**", "/favicon.ico", "/webjars/**"
    };

    @Bean @Order(2)
    public SecurityFilterChain adminApiChain(HttpSecurity http) throws Exception {
        http.securityMatcher("/api/admin/**")
            .authorizeHttpRequests(a -> a.anyRequest().hasAuthority("SCOPE_pragmia:admin"))
            .oauth2ResourceServer(o -> o.jwt(j -> {}))
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .csrf(c -> c.disable());
        return http.build();
    }

    @Bean @Order(3)
    public SecurityFilterChain defaultChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(a -> a
                .requestMatchers(PUBLIC_PATHS).permitAll()
                .anyRequest().authenticated())
            .formLogin(f -> f
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/", true)
                .failureUrl("/login?error=true")
                .permitAll())
            .logout(l -> l
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("SESSION", "JSESSIONID")
                .permitAll())
            .csrf(c -> c.ignoringRequestMatchers("/api/**"));
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
