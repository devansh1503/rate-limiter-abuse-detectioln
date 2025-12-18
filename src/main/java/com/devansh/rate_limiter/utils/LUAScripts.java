package com.devansh.rate_limiter.utils;

import com.devansh.rate_limiter.enums.Algorithm;

public class LUAScripts {
    public static String getLua(Algorithm algorithm) {
        if(algorithm == Algorithm.SLIDING_WINDOW_COUNTER){
            return getSlidingWindowLua();
        }
        return getTokenBucketLua();
    }

    private static String getSlidingWindowLua() {
        return "-- KEYS[1] = current window key\n" +
                "-- KEYS[2] = previous window key\n" +
                "-- ARGV[1] = limit\n" +
                "-- ARGV[2] = window_size_millis\n" +
                "-- ARGV[3] = now_millis\n" +
                "\n" +
                "local currentCount = tonumber(redis.call(\"GET\", KEYS[1]) or \"0\")\n" +
                "local previousCount = tonumber(redis.call(\"GET\", KEYS[2]) or \"0\")\n" +
                "\n" +
                "local limit = tonumber(ARGV[1])\n" +
                "local windowSize = tonumber(ARGV[2])\n" +
                "local now = tonumber(ARGV[3])\n" +
                "\n" +
                "-- Calculate current window start\n" +
                "local windowStart = now - (now % windowSize)\n" +
                "local elapsed = now - windowStart\n" +
                "\n" +
                "-- Sliding window weight\n" +
                "local weight = 1 - (elapsed / windowSize)\n" +
                "\n" +
                "-- Estimated request count\n" +
                "local estimated = previousCount * weight + currentCount\n" +
                "\n" +
                "if estimated >= limit then\n" +
                "    return 0\n" +
                "end\n" +
                "\n" +
                "-- Increment current window\n" +
                "redis.call(\"INCR\", KEYS[1])\n" +
                "redis.call(\"PEXPIRE\", KEYS[1], windowSize * 2)\n" +
                "\n" +
                "return 1";
    }

    private static String getTokenBucketLua() {
        return "-- KEYS[1] = bucket key\n" +
                "-- ARGV[1] = capacity\n" +
                "-- ARGV[2] = refill_rate_per_millis\n" +
                "-- ARGV[3] = now\n" +
                "\n" +
                "local data = redis.call(\"HMGET\", KEYS[1], \"tokens\", \"last_refill\")\n" +
                "local tokens = tonumber(data[1])\n" +
                "local lastRefill = tonumber(data[2])\n" +
                "\n" +
                "if tokens == nil then\n" +
                "    tokens = tonumber(ARGV[1])\n" +
                "    lastRefill = tonumber(ARGV[3])\n" +
                "end\n" +
                "\n" +
                "local elapsed = tonumber(ARGV[3]) - lastRefill\n" +
                "if elapsed > 0 then\n" +
                "    tokens = math.min(tonumber(ARGV[1]),\n" +
                "        tokens + elapsed * tonumber(ARGV[2]))\n" +
                "    lastRefill = tonumber(ARGV[3])\n" +
                "end\n" +
                "\n" +
                "if tokens < 1 then\n" +
                "    redis.call(\"HMSET\", KEYS[1], \"tokens\", tokens, \"last_refill\", lastRefill)\n" +
                "    redis.call(\"PEXPIRE\", KEYS[1], 60000)\n" +
                "    return 0\n" +
                "end\n" +
                "\n" +
                "tokens = tokens - 1\n" +
                "redis.call(\"HMSET\", KEYS[1], \"tokens\", tokens, \"last_refill\", lastRefill)\n" +
                "redis.call(\"PEXPIRE\", KEYS[1], 60000)\n" +
                "\n" +
                "return 1";
    }
}
