package com.nayeem.redistest.service;

import com.nayeem.redistest.model.TestData;
import com.nayeem.redistest.repository.TestDataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
@Slf4j
public class SpringDataPerformanceTestService {

    private final TestDataRepository testDataRepository;
    
    private final AtomicBoolean testRunning = new AtomicBoolean(false);
    private final AtomicLong insertedCount = new AtomicLong(0);
    private final AtomicLong totalInserted = new AtomicLong(0);

    public void startPerformanceTest(int totalRecords) {
        if (testRunning.get()) {
            log.warn("Spring Data performance test is already running!");
            return;
        }

        testRunning.set(true);
        insertedCount.set(0);
        totalInserted.set(0);

        log.info("Starting Spring Data performance test with {} records", totalRecords);

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
            log.info("Spring Data insertion completed. Total inserted: {}", totalInserted.get());
            testRunning.set(false);
        });

        // Wait for monitoring to complete (it will stop when test is done)
        monitoringTask.thenRun(() -> {
            log.info("Spring Data monitoring completed");
        });
    }

    private void insertDataInBatches(int totalRecords) {
        long startTime = System.currentTimeMillis();
        
        for (int i = 1; i <= totalRecords; i++) {
            try {
                String id = "perf-test-" + String.format("%06d", i);
                TestData testData = new TestData(
                    id,
                    "Performance Test Data " + i,
                    "This is Spring Data performance test data entry number " + i,
                    LocalDateTime.now(),
                    i
                );

                testDataRepository.save(testData);
                
                insertedCount.incrementAndGet();
                totalInserted.incrementAndGet();

                // Log progress every 1000 records
                if (i % 1000 == 0) {
                    long currentTime = System.currentTimeMillis();
                    double rate = (double) i / ((currentTime - startTime) / 1000.0);
                    log.info("Spring Data - Inserted {} records. Rate: {:.2f} records/sec", i, rate);
                }

            } catch (Exception e) {
                log.error("Error inserting record {}: {}", i, e.getMessage());
            }
        }
        
        long endTime = System.currentTimeMillis();
        double totalRate = (double) totalRecords / ((endTime - startTime) / 1000.0);
        log.info("Spring Data insertion completed in {} ms. Average rate: {:.2f} records/sec", 
                (endTime - startTime), totalRate);
    }

    private void monitorDataCount() {
        long startTime = System.currentTimeMillis();
        int monitoringCount = 0;
        
        while (testRunning.get()) {
            try {
                long currentCount = testDataRepository.count();
                long currentTime = System.currentTimeMillis();
                monitoringCount++;
                
                log.info("Spring Data Monitoring #{} - Total records in Redis: {} (elapsed: {} ms)", 
                        monitoringCount, currentCount, (currentTime - startTime));
                
                Thread.sleep(500); // Wait 0.5 seconds
                
            } catch (InterruptedException e) {
                log.warn("Spring Data monitoring thread interrupted");
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.error("Error during Spring Data monitoring: {}", e.getMessage());
            }
        }
        
        // Final count after insertion is complete
        long finalCount = testDataRepository.count();
        log.info("Spring Data final count after test completion: {}", finalCount);
    }

    public long getTotalDataCount() {
        try {
            return testDataRepository.count();
        } catch (Exception e) {
            log.error("Error getting total count: {}", e.getMessage());
            return 0;
        }
    }

    public long getPerformanceTestDataCount() {
        try {
            return testDataRepository.countByNameStartingWith("Performance Test Data");
        } catch (Exception e) {
            log.error("Error getting performance test count: {}", e.getMessage());
            return 0;
        }
    }

    public void clearPerformanceTestData() {
        try {
            long count = testDataRepository.countByNameStartingWith("Performance Test Data");
            if (count > 0) {
                testDataRepository.deleteAll(testDataRepository.findByNameStartingWith("Performance Test Data"));
                log.info("Cleared {} Spring Data performance test records", count);
            }
        } catch (Exception e) {
            log.error("Error clearing Spring Data performance test data: {}", e.getMessage());
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
