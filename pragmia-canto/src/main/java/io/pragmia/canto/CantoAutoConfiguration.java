package io.pragmia.canto;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@AutoConfiguration
@ComponentScan("io.pragmia.canto")
@EntityScan("io.pragmia.canto.model")
@EnableJpaRepositories("io.pragmia.canto.repository")
public class CantoAutoConfiguration {}
