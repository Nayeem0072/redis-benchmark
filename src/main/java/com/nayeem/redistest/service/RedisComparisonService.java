package com.nayeem.redistest.service;

import com.nayeem.redistest.model.TestData;
import com.nayeem.redistest.repository.TestDataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisComparisonService {

    private final RedisTemplate<String, TestData> redisTemplate;
    private final TestDataRepository testDataRepository;
    
    private final AtomicBoolean testRunning = new AtomicBoolean(false);
    private final AtomicLong redisTemplateInsertedCount = new AtomicLong(0);
    private final AtomicLong springDataInsertedCount = new AtomicLong(0);

    public void startComparisonTest(int totalRecords) {
        if (testRunning.compareAndSet(false, true)) {
            log.info("Starting Redis comparison test with {} records", totalRecords);
            
            // Reset counters
            redisTemplateInsertedCount.set(0);
            springDataInsertedCount.set(0);
            
            // Start insertion threads for both approaches on the same Redis server
            CompletableFuture<Void> redisTemplateInsertion = CompletableFuture.runAsync(() -> 
                insertDataWithRedisTemplate(redisTemplate, "redistemplate", totalRecords, redisTemplateInsertedCount));
            
            CompletableFuture<Void> springDataInsertion = CompletableFuture.runAsync(() -> 
                insertDataWithSpringData(totalRecords, springDataInsertedCount));
            
            // Start monitoring thread
            CompletableFuture<Void> monitoring = CompletableFuture.runAsync(() -> 
                monitorAllApproaches(totalRecords));
            
            // Wait for all insertions to complete
            CompletableFuture.allOf(redisTemplateInsertion, springDataInsertion)
                .thenRun(() -> {
                    testRunning.set(false);
                    log.info("Redis comparison test completed");
                });
        } else {
            log.warn("Comparison test is already running");
        }
    }

    private void insertDataWithRedisTemplate(RedisTemplate<String, TestData> template, String serverName, 
                                           int totalRecords, AtomicLong counter) {
        long startTime = System.currentTimeMillis();
        String keyPrefix = "comparison:" + serverName + ":";
        
        try {
            for (int i = 1; i <= totalRecords; i++) {
                String id = serverName + "-" + i;
                TestData testData = new TestData(id, "Comparison Test Data", 
                    "Testing " + serverName + " Redis server", LocalDateTime.now(), i);
                
                String key = keyPrefix + id;
                template.opsForValue().set(key, testData);
                counter.incrementAndGet();
                
                if (i % 1000 == 0) {
                    long elapsed = System.currentTimeMillis() - startTime;
                    double rate = (double) i / elapsed * 1000;
                    log.info("{} Redis - Inserted {} records. Rate: {:.2f} records/sec", 
                        serverName, i, rate);
                }
            }
            
            long totalTime = System.currentTimeMillis() - startTime;
            double avgRate = (double) totalRecords / totalTime * 1000;
            log.info("{} Redis insertion completed in {} ms. Average rate: {:.2f} records/sec", 
                serverName, totalTime, avgRate);
                
        } catch (Exception e) {
            log.error("Error inserting data to {} Redis: {}", serverName, e.getMessage());
        }
    }

    private void insertDataWithSpringData(int totalRecords, AtomicLong counter) {
        long startTime = System.currentTimeMillis();
        
        try {
            for (int i = 1; i <= totalRecords; i++) {
                String id = "springdata-" + i;
                TestData testData = new TestData(id, "Spring Data Test", 
                    "Testing Spring Data Redis", LocalDateTime.now(), i);
                
                testDataRepository.save(testData);
                counter.incrementAndGet();
                
                if (i % 1000 == 0) {
                    long elapsed = System.currentTimeMillis() - startTime;
                    double rate = (double) i / elapsed * 1000;
                    log.info("Spring Data - Inserted {} records. Rate: {:.2f} records/sec", i, rate);
                }
            }
            
            long totalTime = System.currentTimeMillis() - startTime;
            double avgRate = (double) totalRecords / totalTime * 1000;
            log.info("Spring Data insertion completed in {} ms. Average rate: {:.2f} records/sec", 
                totalTime, avgRate);
                
        } catch (Exception e) {
            log.error("Error inserting data with Spring Data: {}", e.getMessage());
        }
    }

    private void monitorAllApproaches(int totalRecords) {
        int monitoringCount = 0;
        
        while (testRunning.get()) {
            try {
                monitoringCount++;
                
                // Count records in RedisTemplate approach
                long redisTemplateCount = getRedisTemplateCount(redisTemplate, "comparison:redistemplate:");
                
                // Count records with Spring Data
                long springDataCount = testDataRepository.count();
                
                long elapsed = System.currentTimeMillis() - System.currentTimeMillis();
                
                log.info("Comparison Monitoring #{} - RedisTemplate: {}, Spring Data: {} (elapsed: {} ms)", 
                    monitoringCount, redisTemplateCount, springDataCount, elapsed);
                
                Thread.sleep(500); // Monitor every 0.5 seconds
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.error("Error during monitoring: {}", e.getMessage());
            }
        }
        
        // Final counts
        long finalRedisTemplateCount = getRedisTemplateCount(redisTemplate, "comparison:redistemplate:");
        long finalSpringDataCount = testDataRepository.count();
        
        log.info("Final comparison counts - RedisTemplate: {}, Spring Data: {}", 
            finalRedisTemplateCount, finalSpringDataCount);
    }

    private long getRedisTemplateCount(RedisTemplate<String, TestData> template, String keyPrefix) {
        try {
            return template.keys(keyPrefix + "*").size();
        } catch (Exception e) {
            log.error("Error counting records: {}", e.getMessage());
            return 0;
        }
    }

    public boolean isTestRunning() {
        return testRunning.get();
    }

    public long getRedisTemplateInsertedCount() {
        return redisTemplateInsertedCount.get();
    }

    public long getSpringDataInsertedCount() {
        return springDataInsertedCount.get();
    }

    public void clearAllTestData() {
        try {
            // Clear RedisTemplate data
            redisTemplate.delete(redisTemplate.keys("comparison:redistemplate:*"));
            
            // Clear Spring Data Redis
            testDataRepository.deleteAll();
            
            log.info("All comparison test data cleared");
        } catch (Exception e) {
            log.error("Error clearing test data: {}", e.getMessage());
        }
    }
}
