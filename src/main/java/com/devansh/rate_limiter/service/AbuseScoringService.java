package com.devansh.rate_limiter.service;

import com.devansh.rate_limiter.model.AbuseEvent;
import io.lettuce.core.api.StatefulRedisConnection;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AbuseScoringService {
    private final StatefulRedisConnection<String, String> connection;
    private final ObjectMapper objectMapper;

    public void process(String json){
        System.out.println(json);
        AbuseEvent event = parse(json);
        String ip = event.getIp();

        connection.sync().incr("abuse:request:"+ip);
        connection.sync().expire("abuse:request:"+ip, 60);

        if(event.getStatus() >= 400){
            System.out.println("ERROR INCR");
            connection.sync().incr("abuse:err:"+ip);
            connection.sync().expire("abuse:err:"+ip, 60);
        }

        int score = calculateScore(ip);
        System.out.println("score:"+score);
        long totalScore = connection.sync().incrby("abuse:score:"+ip, score);

        if(totalScore == score) {
            connection.sync().expire("abuse:score:"+ip, 900);
        }

        if(totalScore > 50){
            connection.sync().setex("abuse:block:"+ip, 900,"1");
        }
    }

    private int calculateScore(String ip){
        var redis = connection.sync();

        long req = getLong(redis.get("abuse:request:"+ip));
        long err = getLong(redis.get("abuse:err:"+ip));

        int score = 0;

        if(req > 100) score += 5;
        if(req > 200) score += 10;

        if(req > 0){
            double errorRate = (double)err/(double)req;
            if(errorRate > 0.5) score += 10;
            if(errorRate > 0.8) score += 20;
        }

        return score;
    }

    private long getLong(String value) {
        return value == null ? 0 : Long.parseLong(value);
    }

    private AbuseEvent  parse(String json){
        return objectMapper.readValue(json, AbuseEvent.class);
    }
}
