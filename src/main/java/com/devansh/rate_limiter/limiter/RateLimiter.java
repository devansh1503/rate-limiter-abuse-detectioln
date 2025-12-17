package com.devansh.rate_limiter.limiter;

public interface RateLimiter {
    public boolean isAllowed(String key);
}
