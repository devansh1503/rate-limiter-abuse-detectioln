package com.devansh.rate_limiter.service;

import com.devansh.rate_limiter.enums.Algorithm;
import com.devansh.rate_limiter.limiter.RateLimiter;
import com.devansh.rate_limiter.limiter.RateLimiterFactory;
import com.devansh.rate_limiter.model.RateLimitResult;
import com.devansh.rate_limiter.model.RateLimitRule;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@RequiredArgsConstructor
public class RateLimitService {
    private final RuleService ruleService;

    public RateLimitResult check(String key, String api){
        RateLimitRule rule = ruleService.getRule(api);
        RateLimiter limiter;
        if(rule.getAlgorithm() == Algorithm.SLIDING_WINDOW_COUNTER){
            limiter = RateLimiterFactory.createSlidingWindow(rule.getLimit(), rule.getWindowMillis());
        }
        else{
            limiter = RateLimiterFactory.createTokenBucket(rule.getLimit(), rule.getWindowMillis());
        }

        boolean allowed = limiter.isAllowed(key);

        return new RateLimitResult(allowed);
    }
}
