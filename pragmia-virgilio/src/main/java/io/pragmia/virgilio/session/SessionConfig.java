package io.pragmia.virgilio.session;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisIndexedHttpSession;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
import org.springframework.session.data.redis.RedisIndexedSessionRepository;

import java.time.Duration;

@Configuration
@EnableRedisIndexedHttpSession(maxInactiveIntervalInSeconds = 1800)
public class SessionConfig {

    @Bean
    public RedisIndexedSessionRepository sessionRepository(RedisConnectionFactory factory) {
        RedisIndexedSessionRepository repo = new RedisIndexedSessionRepository(
            org.springframework.data.redis.core.RedisTemplate
                .builder(factory)
                .build());
        repo.setDefaultMaxInactiveInterval(Duration.ofMinutes(30));
        return repo;
    }
}
