package io.pragmia.virgilio.session;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisIndexedHttpSession;
import org.springframework.session.data.redis.RedisIndexedSessionRepository;

import java.time.Duration;

@Configuration
@EnableRedisIndexedHttpSession(maxInactiveIntervalInSeconds = 1800)
public class SessionConfig {

    @Bean
    public RedisTemplate<String, Object> sessionRedisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public RedisIndexedSessionRepository sessionRepository(
            RedisTemplate<String, Object> sessionRedisTemplate) {
        RedisIndexedSessionRepository repo = new RedisIndexedSessionRepository(sessionRedisTemplate);
        repo.setDefaultMaxInactiveInterval(Duration.ofMinutes(30));
        return repo;
    }
}
