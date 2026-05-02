package io.pragmia;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "io.pragmia")
@EntityScan(basePackages = "io.pragmia")
@EnableScheduling
public class PragmiaApplication {
    public static void main(String[] args) {
        SpringApplication.run(PragmiaApplication.class, args);
    }
}
