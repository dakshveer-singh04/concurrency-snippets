/*
 * Copyright (C) 2025, OpenLearn
 * All rights reserved.
 */
package com.openlearn.concurrency;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Supplier;

public class CacheCoherenceBenchmark {

    private static final int NUM_THREADS = 4;
    private static final int NUM_ITERATIONS = 10_00_000;
    private static final AtomicInteger atomicCounter = new AtomicInteger(0);
    private static final LongAdder longAdder = new LongAdder();

    public static void main(String[] args) throws InterruptedException {
        benchmark_action("AtomicInteger", false, atomicCounter::incrementAndGet, atomicCounter::get);
        atomicCounter.set(0);
        benchmark_action("AtomicInteger", true, atomicCounter::incrementAndGet, atomicCounter::get);

        benchmark_action("LongAdder", false, longAdder::increment, longAdder::longValue);
        longAdder.reset();
        benchmark_action("LongAdder", true, longAdder::increment, longAdder::longValue);
    }

    private static void benchmark_action(String label, boolean sameCore, Runnable action, Supplier<?> getValue) throws InterruptedException {
        System.out.println("Benchmarking " + label + " with sameCore = " + sameCore);
        long startTime = System.nanoTime();
        CountDownLatch latch = new CountDownLatch(NUM_THREADS);

        if (sameCore) {
            try (ExecutorService executor = Executors.newSingleThreadExecutor()) {
                for (int i = 0; i < NUM_THREADS; i++) {
                    executor.submit(
                            () -> {
                                for (int j = 0; j < NUM_ITERATIONS; j++) {
                                    action.run();
                                }
                                latch.countDown();
                            });
                }
                executor.shutdown();
            }
        } else {
            for (int i = 0; i < NUM_THREADS; i++) {
                new Thread(
                        () -> {
                            for (int j = 0; j < NUM_ITERATIONS; j++) {
                                action.run();
                            }
                            latch.countDown();
                        })
                        .start();
            }
        }

        latch.await();
        long endTime = System.nanoTime();
        System.out.println("Time taken: " + (endTime - startTime) + "ns  to count : " + getValue.get() + "\n");
    }
}
