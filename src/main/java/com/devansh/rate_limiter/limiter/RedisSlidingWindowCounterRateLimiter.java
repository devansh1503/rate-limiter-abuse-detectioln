package com.devansh.rate_limiter.limiter;

import io.lettuce.core.ScriptOutputType;
import io.lettuce.core.api.sync.RedisCommands;
import org.apache.kafka.common.protocol.types.Field;

public class RedisSlidingWindowCounterRateLimiter implements RateLimiter {
    RedisCommands<String, String> redis;
    private final int limit;
    private final long windowSizeMillis;
    private final String luaScript;

    public RedisSlidingWindowCounterRateLimiter(RedisCommands<String, String> redis, int limit, long windowSizeMillis, String luaScript) {
        this.redis = redis;
        this.limit = limit;
        this.windowSizeMillis = windowSizeMillis;
        this.luaScript = luaScript;
    }
    @Override
    public boolean isAllowed(String key) {
        long now = System.currentTimeMillis();

        long windowStart = now - (now % windowSizeMillis);
        long prevWindowStart = windowStart - windowSizeMillis;

        String currentKey = "sw:" + key + ":" + windowStart;
        String previousKey = "sw:" + key + ":" + prevWindowStart;

        Long result = redis.eval(
                luaScript,
                ScriptOutputType.INTEGER,
                new String[]{currentKey, previousKey},
                String.valueOf(limit),
                String.valueOf(windowSizeMillis),
                String.valueOf(now)
        );

        return result != null && result == 1;
    }
}
