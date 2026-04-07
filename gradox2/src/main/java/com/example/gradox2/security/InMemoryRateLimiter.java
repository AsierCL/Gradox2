package com.example.gradox2.security;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

@Component
public class InMemoryRateLimiter {

    private final ConcurrentHashMap<String, Deque<Long>> requestBuckets = new ConcurrentHashMap<>();

    public boolean isAllowed(String key, int maxRequests, long windowMs) {
        long now = System.currentTimeMillis();
        Deque<Long> bucket = requestBuckets.computeIfAbsent(key, ignored -> new ArrayDeque<>());

        synchronized (bucket) {
            while (!bucket.isEmpty() && now - bucket.peekFirst() > windowMs) {
                bucket.pollFirst();
            }

            if (bucket.size() >= maxRequests) {
                return false;
            }

            bucket.addLast(now);
            return true;
        }
    }
}