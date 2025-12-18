package com.devansh.rate_limiter.service;

import com.devansh.rate_limiter.enums.Algorithm;
import com.devansh.rate_limiter.model.RateLimitRule;
import io.lettuce.core.api.StatefulRedisConnection;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

@Service
@AllArgsConstructor
@RequiredArgsConstructor
public class RuleService {

    private final StatefulRedisConnection<String, String> connection;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public RateLimitRule getRule(String api){
        String json = connection.sync().get(api);
        if(json == null){
            return new RateLimitRule(5, 60_000, Algorithm.TOKEN_BUCKET);
        }

        try {
            return objectMapper.readValue(json, RateLimitRule.class);
        } catch (Exception e) {
            throw new RuntimeException("Invalid rule config for API: " + api, e);
        }
    }
}
