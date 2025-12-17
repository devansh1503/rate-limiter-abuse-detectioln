package com.devansh.rate_limiter.service;

import com.devansh.rate_limiter.enums.Algorithm;
import com.devansh.rate_limiter.model.RateLimitRule;
import org.springframework.stereotype.Service;

@Service
public class RuleService {
    public RateLimitRule getRule(String api){
        return new RateLimitRule(5, 60_000, Algorithm.SLIDING_WINDOW_COUNTER);
    }
}
