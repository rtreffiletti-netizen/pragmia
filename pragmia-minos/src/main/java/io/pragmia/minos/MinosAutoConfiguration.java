package io.pragmia.minos;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@AutoConfiguration
@ComponentScan("io.pragmia.minos")
@EntityScan("io.pragmia.minos.model")
@EnableJpaRepositories("io.pragmia.minos.repository")
public class MinosAutoConfiguration {}
