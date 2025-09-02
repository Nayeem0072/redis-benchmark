package com.nayeem.redistest.service;

import com.nayeem.redistest.model.TestData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
@Slf4j
public class PerformanceTestService {

    private final RedisTemplate<String, TestData> redisTemplate;
    private static final String KEY_PREFIX = "testdata:";
    private static final String PERFORMANCE_TEST_PREFIX = "perf-test:";
    
    private final AtomicBoolean testRunning = new AtomicBoolean(false);
    private final AtomicLong insertedCount = new AtomicLong(0);
    private final AtomicLong totalInserted = new AtomicLong(0);

    public void startPerformanceTest(int totalRecords) {
        if (testRunning.get()) {
            log.warn("Performance test is already running!");
            return;
        }

        testRunning.set(true);
        insertedCount.set(0);
        totalInserted.set(0);

        log.info("Starting performance test with {} records", totalRecords);

        // Start data insertion thread
        CompletableFuture<Void> insertionTask = CompletableFuture.runAsync(() -> {
            insertDataInBatches(totalRecords);
        });

        // Start count monitoring thread
        CompletableFuture<Void> monitoringTask = CompletableFuture.runAsync(() -> {
            monitorDataCount();
        });

        // Wait for insertion to complete
        insertionTask.thenRun(() -> {
            log.info("Data insertion completed. Total inserted: {}", totalInserted.get());
            testRunning.set(false);
        });

        // Wait for monitoring to complete (it will stop when test is done)
        monitoringTask.thenRun(() -> {
            log.info("Monitoring completed");
        });
    }

    private void insertDataInBatches(int totalRecords) {
        long startTime = System.currentTimeMillis();
        
        for (int i = 1; i <= totalRecords; i++) {
            try {
                String id = PERFORMANCE_TEST_PREFIX + String.format("%06d", i);
                TestData testData = new TestData(
                    id,
                    "Performance Test Data " + i,
                    "This is performance test data entry number " + i,
                    LocalDateTime.now(),
                    i
                );

                String key = KEY_PREFIX + id;
                redisTemplate.opsForValue().set(key, testData, 1, java.util.concurrent.TimeUnit.HOURS);
                
                insertedCount.incrementAndGet();
                totalInserted.incrementAndGet();

                // Log progress every 1000 records
                if (i % 1000 == 0) {
                    long currentTime = System.currentTimeMillis();
                    double rate = (double) i / ((currentTime - startTime) / 1000.0);
                    log.info("Inserted {} records. Rate: {:.2f} records/sec", i, rate);
                }

            } catch (Exception e) {
                log.error("Error inserting record {}: {}", i, e.getMessage());
            }
        }
        
        long endTime = System.currentTimeMillis();
        double totalRate = (double) totalRecords / ((endTime - startTime) / 1000.0);
        log.info("Insertion completed in {} ms. Average rate: {:.2f} records/sec", 
                (endTime - startTime), totalRate);
    }

    private void monitorDataCount() {
        long startTime = System.currentTimeMillis();
        int monitoringCount = 0;
        
        while (testRunning.get()) {
            try {
                long currentCount = getTotalDataCount();
                long currentTime = System.currentTimeMillis();
                monitoringCount++;
                
                log.info("Monitoring #{} - Total records in Redis: {} (elapsed: {} ms)", 
                        monitoringCount, currentCount, (currentTime - startTime));
                
                Thread.sleep(500); // Wait 0.5 seconds
                
            } catch (InterruptedException e) {
                log.warn("Monitoring thread interrupted");
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.error("Error during monitoring: {}", e.getMessage());
            }
        }
        
        // Final count after insertion is complete
        long finalCount = getTotalDataCount();
        log.info("Final count after test completion: {}", finalCount);
    }

    public long getTotalDataCount() {
        try {
            java.util.Set<String> keys = redisTemplate.keys(KEY_PREFIX + "*");
            return keys != null ? keys.size() : 0;
        } catch (Exception e) {
            log.error("Error getting total count: {}", e.getMessage());
            return 0;
        }
    }

    public long getPerformanceTestDataCount() {
        try {
            java.util.Set<String> keys = redisTemplate.keys(KEY_PREFIX + PERFORMANCE_TEST_PREFIX + "*");
            return keys != null ? keys.size() : 0;
        } catch (Exception e) {
            log.error("Error getting performance test count: {}", e.getMessage());
            return 0;
        }
    }

    public void clearPerformanceTestData() {
        try {
            java.util.Set<String> keys = redisTemplate.keys(KEY_PREFIX + PERFORMANCE_TEST_PREFIX + "*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.info("Cleared {} performance test records", keys.size());
            }
        } catch (Exception e) {
            log.error("Error clearing performance test data: {}", e.getMessage());
        }
    }

    public boolean isTestRunning() {
        return testRunning.get();
    }

    public long getInsertedCount() {
        return insertedCount.get();
    }

    public long getTotalInserted() {
        return totalInserted.get();
    }
}
