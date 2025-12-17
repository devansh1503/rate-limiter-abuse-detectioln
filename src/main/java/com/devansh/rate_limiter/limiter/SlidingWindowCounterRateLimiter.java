package com.devansh.rate_limiter.limiter;

import java.util.concurrent.ConcurrentHashMap;

public class SlidingWindowCounterRateLimiter implements RateLimiter {
    private static class Window{
        long windowStart;
        int count;
    }
    private final int limit;
    private final long windowSizeMillis;

    private final ConcurrentHashMap<String, Window[]> store = new ConcurrentHashMap<>();

    public SlidingWindowCounterRateLimiter(int limit, long windowSizeMillis) {
        this.limit = limit;
        this.windowSizeMillis = windowSizeMillis;
    }

    @Override
    public boolean isAllowed(String key) {
        long now = System.currentTimeMillis();
        long currentWindowStart = now - (now % windowSizeMillis);

        store.putIfAbsent(key, new Window[]{new Window(), new Window()});

        Window[] windows = store.get(key);

        synchronized (windows) {
            Window current = windows[0];
            Window previous = windows[1];

            if(current.windowStart != currentWindowStart){
                previous.windowStart = current.windowStart;
                previous.count = current.count;

                current.windowStart = currentWindowStart;
                current.count = 0;
            }

            double elapsed = (double) (now - currentWindowStart);
            double weight = 1 - (elapsed / windowSizeMillis);

            double estimatedCount = previous.count * weight + current.count;

            if(estimatedCount < limit){
                current.count++;
                return true;
            }
            return false;
        }

    }
}
