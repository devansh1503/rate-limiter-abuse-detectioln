package com.devansh.rate_limiter.limiter;

public class RateLimiterFactory {
    public static RateLimiter createSlidingWindow(int limit, long windowSizeMillis) {
        return new SlidingWindowCounterRateLimiter(limit, windowSizeMillis);
    }
    public static RateLimiter createTokenBucket(int capacity, long refillRatePerSecond) {
        return new TokenBucketRateLimiter(capacity, refillRatePerSecond);
    }
}