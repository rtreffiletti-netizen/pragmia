package io.pragmia.clio;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@AutoConfiguration
@ComponentScan("io.pragmia.clio")
@EntityScan("io.pragmia.clio.model")
@EnableJpaRepositories("io.pragmia.clio.repository")
public class ClioAutoConfiguration {}
