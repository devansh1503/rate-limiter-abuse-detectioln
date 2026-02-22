package com.devansh.rate_limiter.limiter;

import io.lettuce.core.ScriptOutputType;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;

public class RedisTokenBucketRateLimiter implements RateLimiter {
    private final RedisCommands<String, String> redis;
    private final long capacity;
    private final double refillRatePerMillis;
    private final String luaScript;
    public RedisTokenBucketRateLimiter(
            RedisCommands<String, String> redis,
            long capacity,
            double refillRatePerMillis,
            String luaScript
    ) {
        this.redis = redis;
        this.capacity = capacity;
        this.refillRatePerMillis = refillRatePerMillis;
        this.luaScript = luaScript;

    }
    @Override
    public boolean isAllowed(String key) {
        long now = System.currentTimeMillis();

        Long result = redis.eval(
                luaScript,
                ScriptOutputType.INTEGER,
                new String[]{"token_bucket:"+key},
                String.valueOf(capacity),
                String.valueOf(refillRatePerMillis),
                String.valueOf(now)
        );

        return result != null && result == 1;
    }
}
