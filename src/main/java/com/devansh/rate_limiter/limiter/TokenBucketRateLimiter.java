package com.devansh.rate_limiter.limiter;

import java.util.concurrent.ConcurrentHashMap;

public class TokenBucketRateLimiter implements RateLimiter {

    private static class Bucket{
        double tokens;
        long lastRefillTimestamp;
        Bucket(double tokens, long lastRefillTimestamp) {
            this.tokens = tokens;
            this.lastRefillTimestamp = lastRefillTimestamp;
        }
    }

    private final long capacity;
    private final long refillRatePerSecond;

    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    public TokenBucketRateLimiter(int capacity, long refillRatePerSecond) {
        this.capacity = capacity;
        this.refillRatePerSecond = refillRatePerSecond;
    }

    @Override
    public boolean isAllowed(String key) {
        long now = System.currentTimeMillis();
        Bucket bucket = buckets.computeIfAbsent(key, k -> new Bucket(capacity, now));

        synchronized (bucket) {
            refill(bucket, now);

            if(bucket.tokens >= 1){
                bucket.tokens -= 1;
                return true;
            }
            return false;
        }
    }

    private void refill(Bucket bucket, long now) {
        long elapsed = now - bucket.lastRefillTimestamp;

        if (elapsed > 0) {
            double tokensToAdd = elapsed * refillRatePerSecond;
            bucket.tokens = Math.min(capacity, bucket.tokens + tokensToAdd);
            bucket.lastRefillTimestamp = now;
        }

    }
}
