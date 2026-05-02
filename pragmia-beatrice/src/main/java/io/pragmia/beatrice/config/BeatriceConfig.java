package io.pragmia.beatrice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class BeatriceConfig {
    @Bean
    public RestTemplate beatriceRestTemplate() { return new RestTemplate(); }
}
