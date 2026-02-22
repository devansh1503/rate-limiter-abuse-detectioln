package com.devansh.rate_limiter.service;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AbuseEventProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public void publish(String topic, String key, String value) {
        try {
            System.out.println("Inside Publish");

            kafkaTemplate
                    .send(topic, key, value)
                    .get();

            System.out.println("Message successfully sent to Kafka");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
