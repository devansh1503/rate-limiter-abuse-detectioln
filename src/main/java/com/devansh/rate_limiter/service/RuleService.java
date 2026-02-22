package com.devansh.rate_limiter.service;

import com.devansh.rate_limiter.enums.Algorithm;
import com.devansh.rate_limiter.model.RateLimitRule;
import com.devansh.rate_limiter.model.RateLimitRuleDB;
import com.devansh.rate_limiter.model.RateLimitRuleRequest;
import com.devansh.rate_limiter.repository.RateLimitRuleRepo;
import io.lettuce.core.api.StatefulRedisConnection;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class RuleService {

    private final StatefulRedisConnection<String, String> connection;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RateLimitRuleRepo rateLimitRuleRepo;

    public ResponseEntity<?> createRule(RateLimitRuleRequest rateLimitRuleRequest) {
        RateLimitRuleDB rateLimitRuleDB = new RateLimitRuleDB();
        var redis = connection.sync();

        //Saving to DB
        rateLimitRuleDB.setLimit(rateLimitRuleRequest.getLimit());
        rateLimitRuleDB.setWindowMillis(rateLimitRuleRequest.getWindowMillis());
        rateLimitRuleDB.setEndpoint(rateLimitRuleRequest.getEndpoint());
        rateLimitRuleDB.setAuthor(rateLimitRuleRequest.getAuthor());
        rateLimitRuleDB.setAlgorithm(
                rateLimitRuleRequest.isUseTokenBucket() ? Algorithm.TOKEN_BUCKET : Algorithm.SLIDING_WINDOW_COUNTER
        );

        rateLimitRuleRepo.save(rateLimitRuleDB);

        //Saving to Redis
        RateLimitRule redisRule = new RateLimitRule(
                rateLimitRuleRequest.getLimit(),
                rateLimitRuleRequest.getWindowMillis(),
                rateLimitRuleRequest.isUseTokenBucket() ? Algorithm.TOKEN_BUCKET : Algorithm.SLIDING_WINDOW_COUNTER
        );
        redis.set(rateLimitRuleRequest.getEndpoint(), objectMapper.writeValueAsString(redisRule));


        return ResponseEntity.ok(rateLimitRuleDB);
    }

    public ResponseEntity<?> updateRule(long id, RateLimitRuleRequest rateLimitRuleRequest) {
        RateLimitRuleDB rateLimitRuleDB = rateLimitRuleRepo.findById(id).get();
        var redis = connection.sync();

        rateLimitRuleDB.setLimit(rateLimitRuleRequest.getLimit());
        rateLimitRuleDB.setWindowMillis(rateLimitRuleRequest.getWindowMillis());
        rateLimitRuleDB.setEndpoint(rateLimitRuleRequest.getEndpoint());
        rateLimitRuleDB.setAuthor(rateLimitRuleRequest.getAuthor());
        rateLimitRuleDB.setAlgorithm(
                rateLimitRuleRequest.isUseTokenBucket() ? Algorithm.TOKEN_BUCKET : Algorithm.SLIDING_WINDOW_COUNTER
        );
        rateLimitRuleRepo.save(rateLimitRuleDB);

        RateLimitRule redisRule = new RateLimitRule(
                rateLimitRuleRequest.getLimit(),
                rateLimitRuleRequest.getWindowMillis(),
                rateLimitRuleRequest.isUseTokenBucket() ? Algorithm.TOKEN_BUCKET : Algorithm.SLIDING_WINDOW_COUNTER
        );
        redis.set(rateLimitRuleRequest.getEndpoint(), objectMapper.writeValueAsString(redisRule));

        return ResponseEntity.ok().build();
    }

    public RateLimitRule getRule(String api){
        String json = connection.sync().get(api);
        System.out.println(json);
        try {
            if(json == null){
                return null;
            }
            return objectMapper.readValue(json, RateLimitRule.class);
        } catch (Exception e) {
            throw new RuntimeException("Invalid rule config for API: " + api, e);
        }
    }
}
