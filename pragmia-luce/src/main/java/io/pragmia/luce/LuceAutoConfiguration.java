package io.pragmia.luce;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@AutoConfiguration
@ComponentScan("io.pragmia.luce")
@EntityScan("io.pragmia.luce.model")
@EnableJpaRepositories("io.pragmia.luce.repository")
public class LuceAutoConfiguration {}
