package com.devansh.rate_limiter.config;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LettuceConfig {

    @Bean
    public RedisClient redisClient() {
        return RedisClient.create(
                RedisURI.builder()
                        .withHost("redis")
                        .withPort(6379)
                        .build()
        );
    }

    @Bean
    public StatefulRedisConnection<String, String> redisConnection(RedisClient redisClient) {
        return redisClient.connect();
    }
}
