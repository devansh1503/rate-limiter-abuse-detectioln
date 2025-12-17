package com.devansh.rate_limiter.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RateLimitResult {
    private boolean allowed;
}
