package io.pragmia.virgilio.session;

import org.springframework.context.annotation.Configuration;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisIndexedHttpSession;

@Configuration
@EnableRedisIndexedHttpSession(maxInactiveIntervalInSeconds = 1800)
public class SessionConfig {
    // Spring Session crea automaticamente il RedisIndexedSessionRepository
    // Non serve definirlo manualmente — lo fa @EnableRedisIndexedHttpSession
}
