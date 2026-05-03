package io.pragmia.saml.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ComponentScan;

@Configuration
@ConditionalOnProperty(name = "pragmia.modules.saml.enabled", havingValue = "true", matchIfMissing = true)
@ComponentScan(basePackages = "io.pragmia.saml")
public class SamlModuleConfig {}
