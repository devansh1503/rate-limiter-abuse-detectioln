package com.devansh.rate_limiter.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AbuseEvent {

    private String ip;
    private String method;
    private String path;
    private int status;
    private long latency;
    private long timestamp;
}