package io.pragmia.virgilio;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;

@AutoConfiguration
@ConditionalOnProperty(prefix = "pragmia.modules", name = "virgilioEnabled",
                       havingValue = "true", matchIfMissing = true)
@ComponentScan(basePackages = "io.pragmia.virgilio")
public class VirgilioAutoConfiguration {}
