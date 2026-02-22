package com.devansh.rate_limiter.limiter;

import com.devansh.rate_limiter.enums.Algorithm;
import com.devansh.rate_limiter.utils.LUAScripts;
import io.lettuce.core.api.sync.RedisCommands;
import org.apache.kafka.common.protocol.types.Field;

public class RateLimiterFactory {
    public static RateLimiter createSlidingWindow(int limit, long windowSizeMillis) {
        return new SlidingWindowCounterRateLimiter(limit, windowSizeMillis);
    }
    public static RateLimiter createTokenBucket(int capacity, long refillRatePerSecond) {
        return new TokenBucketRateLimiter(capacity, refillRatePerSecond);
    }
    public static RateLimiter createTokenBucketRedis(
            RedisCommands<String, String> redis,
            long capacity,
            long windowMillis
    ) {
        String LUA_SCRIPT = LUAScripts.getLua(Algorithm.TOKEN_BUCKET);
        double refillRatePerMillis = (double) capacity / windowMillis;
        return new RedisTokenBucketRateLimiter(
                redis,
                capacity,
                refillRatePerMillis,
                LUA_SCRIPT
        );
    }
    public static RateLimiter createSlidingWindowRedis(
            RedisCommands<String, String> redis,
            int limit,
            long windowSizeMillis
    ){
        String LUA_SCRIPT = LUAScripts.getLua(Algorithm.SLIDING_WINDOW_COUNTER);
        return new RedisSlidingWindowCounterRateLimiter(
                redis,
                limit,
                windowSizeMillis,
                LUA_SCRIPT
        );
    }
}