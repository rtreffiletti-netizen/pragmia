package io.pragmia.beatrice;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@AutoConfiguration
@ComponentScan("io.pragmia.beatrice")
@EntityScan("io.pragmia.beatrice.model")
@EnableJpaRepositories("io.pragmia.beatrice.repository")
public class BeatriceAutoConfiguration {}
