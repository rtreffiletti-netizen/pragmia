package io.pragmia.virgilio.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class BruteForceService {

    private final StringRedisTemplate redis;
    private static final String ATTEMPTS_KEY = "pragmia:login:attempts:";
    private static final String LOCK_KEY = "pragmia:login:lock:";
    private static final int MAX_ATTEMPTS = 5;
    private static final Duration LOCKOUT = Duration.ofMinutes(15);
    private static final Duration COOLDOWN = Duration.ofMinutes(10);

    public boolean isLocked(String username) {
        return Boolean.TRUE.equals(redis.hasKey(LOCK_KEY + username));
    }

    public void recordAttempt(String username) {
        String attemptsKey = ATTEMPTS_KEY + username;
        Long attempts = redis.opsForValue().increment(attemptsKey);
        redis.expire(attemptsKey, COOLDOWN);
        if (attempts != null && attempts >= MAX_ATTEMPTS) {
            redis.opsForValue().set(LOCK_KEY + username, "locked", LOCKOUT);
            log.warn("Account locked due to brute force: {}", username);
        }
    }

    public void resetAttempts(String username) {
        redis.delete(ATTEMPTS_KEY + username);
        redis.delete(LOCK_KEY + username);
    }

    public int getAttempts(String username) {
        String val = redis.opsForValue().get(ATTEMPTS_KEY + username);
        return val != null ? Integer.parseInt(val) : 0;
    }

    public long getLockRemainingMs(String username) {
        return redis.getExpire(LOCK_KEY + username, TimeUnit.MILLISECONDS);
    }
}
