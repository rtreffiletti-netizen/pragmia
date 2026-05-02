package io.pragmia.soglia.ratelimit;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;

@Component
@RequiredArgsConstructor
@Slf4j
public class RateLimitFilter extends OncePerRequestFilter {

    private final StringRedisTemplate redis;
    private static final int MAX_REQUESTS_PER_MINUTE = 120;

    @Override
    protected void doFilterInternal(HttpServletRequest req,
                                    HttpServletResponse res,
                                    FilterChain chain) throws ServletException, IOException {
        String ip  = req.getRemoteAddr();
        String key = "rl:" + ip;
        Long count = redis.opsForValue().increment(key);
        if (count != null && count == 1) {
            redis.expire(key, Duration.ofMinutes(1));
        }
        if (count != null && count > MAX_REQUESTS_PER_MINUTE) {
            log.warn("[SOGLIA] Rate limit exceeded for IP {}", ip);
            res.setStatus(429);
            res.getWriter().write("Too Many Requests");
            return;
        }
        chain.doFilter(req, res);
    }
}
