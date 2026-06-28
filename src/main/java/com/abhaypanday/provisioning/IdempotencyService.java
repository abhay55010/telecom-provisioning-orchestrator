package com.abhaypanday.provisioning;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
public class IdempotencyService {

    private static final Logger log = LoggerFactory.getLogger(IdempotencyService.class);
    private static final Duration TTL = Duration.ofHours(24);

    private final StringRedisTemplate redis;

    public IdempotencyService(StringRedisTemplate redis) {
        this.redis = redis;
    }

    public boolean firstTime(String consumer, UUID eventId) {
        String key = "idem:" + consumer + ":" + eventId;
        boolean claimed = Boolean.TRUE.equals(redis.opsForValue().setIfAbsent(key, "1", TTL));
        if (!claimed) {
            log.info("Skipping duplicate event {} for consumer '{}'", eventId, consumer);
        }
        return claimed;
    }
}
