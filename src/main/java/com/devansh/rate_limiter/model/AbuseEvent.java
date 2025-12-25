package com.devansh.rate_limiter.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AbuseEvent {
    private String ip;
    private String method;
    private String path;
    private int status;
    private long latency;
    private long timestamp;
}
