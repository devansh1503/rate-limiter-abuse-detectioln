package com.devansh.rate_limiter.service;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AbuseEventConsumer {

    private final AbuseScoringService scoreService;

    @KafkaListener(topics = "abuse-events", groupId = "abuse-detector-v2")
    public void consume(String message) {
        System.out.println("Inside consumer message: " + message);
        scoreService.process(message);
    }
}
