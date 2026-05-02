package io.pragmia.virgilio.access.config;

import io.pragmia.virgilio.access.service.RiskEngine;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(
    prefix = "pragmia.virgilio.conditional-access",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = true
)
public class ConditionalAccessAutoConfiguration {

    @Bean
    public RiskEngine riskEngine() {
        return new RiskEngine();
    }
}
