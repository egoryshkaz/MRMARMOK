package com.example.ANONIMUS.service;

import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicLong;

@Service
public class RequestCounterService {
    private final AtomicLong counter = new AtomicLong(0);


    public synchronized void increment() {
        counter.incrementAndGet();
    }


    public synchronized long getCount() {
        return counter.get();
    }

    public synchronized void reset() {
        counter.set(0);
    }
}