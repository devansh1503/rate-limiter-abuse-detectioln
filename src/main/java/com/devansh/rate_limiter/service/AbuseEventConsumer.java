package com.devansh.rate_limiter.service;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AbuseEventConsumer {

    private final AbuseScoringService scoreService;

    @KafkaListener(topics = "abuse-events", groupId = "abuse-detector")
    public void consume(String message) {
        scoreService.process(message);
    }
}
