package com.devansh.rate_limiter.model;

import com.devansh.rate_limiter.enums.Algorithm;
import lombok.Data;

@Data
public class RateLimitRuleRequest {
    private int limit;
    private long windowMillis;
    private String endpoint;
    private String author;
    private boolean useTokenBucket;
}
