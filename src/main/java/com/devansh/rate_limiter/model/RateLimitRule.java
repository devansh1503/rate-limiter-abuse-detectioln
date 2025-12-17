package com.devansh.rate_limiter.model;

import com.devansh.rate_limiter.enums.Algorithm;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RateLimitRule {
    private int limit;
    private long windowMillis;
    private Algorithm algorithm;
}
