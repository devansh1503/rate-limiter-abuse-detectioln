package com.devansh.rate_limiter.service;

import com.devansh.rate_limiter.enums.Algorithm;
import com.devansh.rate_limiter.limiter.RateLimiter;
import com.devansh.rate_limiter.limiter.RateLimiterFactory;
import com.devansh.rate_limiter.model.RateLimitResult;
import com.devansh.rate_limiter.model.RateLimitRule;
import io.lettuce.core.api.StatefulRedisConnection;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class RateLimitService {
    private final RuleService ruleService;
    private final StatefulRedisConnection<String, String> connection;

    private final Map<String, RateLimiter> cache = new ConcurrentHashMap<>(); //To Reuse the same Object

    public RateLimitResult check(String key, String api){

        RateLimitRule rule = ruleService.getRule(api);
        System.out.println(rule.toString());
        if(rule == null){
            return new RateLimitResult(true);
        }

        RateLimiter limiter;

        String rateLimiterClassKey = api+"-"+rule.getAlgorithm();

        if(rule.getAlgorithm() == Algorithm.SLIDING_WINDOW_COUNTER){
            limiter = cache.computeIfAbsent(rateLimiterClassKey, a->
                    RateLimiterFactory.createSlidingWindowRedis(
                            connection.sync(),
                            rule.getLimit(),
                            rule.getWindowMillis()
                    )
            );
        }
        else{
            limiter = cache.computeIfAbsent(rateLimiterClassKey, a->
                    RateLimiterFactory.createTokenBucketRedis(
                            connection.sync(),
                            rule.getLimit(),
                            rule.getWindowMillis()
                    )
            );
//            limiter = RateLimiterFactory.createTokenBucket(rule.getLimit(), rule.getWindowMillis());
        }

        System.out.println(limiter);
        boolean allowed = limiter.isAllowed(key);

        return new RateLimitResult(allowed);
    }

    public boolean isAbuseBlock(String ip){
        String key = "abuse:block:" + ip;
        return connection.sync().exists(key) > 0;
    }
}
