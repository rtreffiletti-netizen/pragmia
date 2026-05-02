package io.pragmia.virgilio;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@AutoConfiguration
@ConditionalOnProperty(prefix = "pragmia.modules", name = "virgilioEnabled",
                       havingValue = "true", matchIfMissing = true)
@ComponentScan(basePackages = "io.pragmia.virgilio")
@EnableJpaRepositories(basePackages = "io.pragmia.virgilio")
@EntityScan(basePackages = "io.pragmia.virgilio")
public class VirgilioAutoConfiguration {}
