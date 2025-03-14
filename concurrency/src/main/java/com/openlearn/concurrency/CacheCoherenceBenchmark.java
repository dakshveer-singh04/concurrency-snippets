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

public class CacheCoherenceBenchmark {

  private static final int NUM_THREADS = 4;
  private static final int NUM_ITERATIONS = 10_00_000;
  private static final AtomicInteger atomicCounter = new AtomicInteger(0);
  private static final LongAdder longAdder = new LongAdder();

  public static void main(String[] args) throws InterruptedException {
    benchmark_atomicInteger("On Multiple cores AtomicInteger", false);
    atomicCounter.set(0);
    benchmark_atomicInteger("On same core AtomicInteger", true);

    benchmark_longAdder("On Multiple cores LongAdder", false);
    longAdder.reset();
    benchmark_longAdder("On same core LongAdder", true);
  }

  private static void benchmark_atomicInteger(String label, boolean sameCore)
      throws InterruptedException {
    System.out.println("Benchmarking " + label + " with sameCore = " + sameCore);
    long startTime = System.nanoTime();
    CountDownLatch latch = new CountDownLatch(NUM_THREADS);

    if (sameCore) {
      try (ExecutorService executor = Executors.newSingleThreadExecutor()) {
        for (int i = 0; i < NUM_THREADS; i++) {
          executor.submit(
              () -> {
                for (int j = 0; j < NUM_ITERATIONS; j++) {
                  atomicCounter.incrementAndGet();
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
                    atomicCounter.incrementAndGet();
                  }
                  latch.countDown();
                })
            .start();
      }
    }

    latch.await();
    long endTime = System.nanoTime();
    System.out.println(
        "Time taken: " + (endTime - startTime) + "ns  to count : " + atomicCounter.get() + "\n");
  }

  private static void benchmark_longAdder(String label, boolean sameCore)
      throws InterruptedException {
    System.out.println("Benchmarking " + label + " with sameCore = " + sameCore);
    long startTime = System.nanoTime();
    CountDownLatch latch = new CountDownLatch(NUM_THREADS);

    if (sameCore) {
      try (ExecutorService executor = Executors.newSingleThreadExecutor()) {
        for (int i = 0; i < NUM_THREADS; i++) {
          executor.submit(
              () -> {
                for (int j = 0; j < NUM_ITERATIONS; j++) {
                  longAdder.increment();
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
                    longAdder.increment();
                  }
                  latch.countDown();
                })
            .start();
      }
    }

    latch.await();
    long endTime = System.nanoTime();
    System.out.println(
        "Time taken: " + (endTime - startTime) + "ns  to count : " + longAdder.longValue() + "\n");
  }
}
