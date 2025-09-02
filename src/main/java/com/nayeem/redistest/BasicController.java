package com.nayeem.redistest;

import com.nayeem.redistest.model.TestData;
import com.nayeem.redistest.service.RedisService;
import com.nayeem.redistest.service.PerformanceTestService;
import com.nayeem.redistest.service.SpringDataRedisService;
import com.nayeem.redistest.service.SpringDataPerformanceTestService;
import com.nayeem.redistest.service.RedisComparisonService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class BasicController {

    private final RedisService redisService;
    private final PerformanceTestService performanceTestService;
    private final SpringDataRedisService springDataRedisService;
    private final SpringDataPerformanceTestService springDataPerformanceTestService;
    private final RedisComparisonService redisComparisonService;

    @PostMapping("/data")
    public ResponseEntity<Map<String, Object>> insertData(@RequestBody TestData testData) {
        try {
            redisService.saveTestData(testData);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Data inserted successfully");
            response.put("id", testData.getId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to insert data: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/data/{id}")
    public ResponseEntity<Map<String, Object>> getData(@PathVariable String id) {
        try {
            TestData data = redisService.getTestData(id);
            Map<String, Object> response = new HashMap<>();
            if (data != null) {
                response.put("success", true);
                response.put("data", data);
            } else {
                response.put("success", false);
                response.put("message", "Data not found for id: " + id);
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to retrieve data: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @DeleteMapping("/data/{id}")
    public ResponseEntity<Map<String, Object>> deleteData(@PathVariable String id) {
        try {
            boolean deleted = redisService.deleteTestData(id);
            Map<String, Object> response = new HashMap<>();
            response.put("success", deleted);
            response.put("message", deleted ? "Data deleted successfully" : "Data not found");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to delete data: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/data")
    public ResponseEntity<Map<String, Object>> saveSampleData() {
        try {
            // Generate random ID
            String randomId = "test-" + java.util.UUID.randomUUID().toString().substring(0, 8);
            
            // Create sample data with random ID
            TestData sampleData = new TestData(randomId, "Sample Data", "Test entry", null, 42);
            
            // Save to Redis
            redisService.saveTestData(sampleData);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Sample data saved successfully");
            response.put("data", sampleData);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to save sample data: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/data/all")
    public ResponseEntity<Map<String, Object>> getAllData() {
        try {
            java.util.List<TestData> allData = redisService.getAllTestData();
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", allData);
            response.put("count", allData.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to retrieve all data: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "Redis Test Application");
        return ResponseEntity.ok(response);
    }

    // Performance Test Endpoints
    @PostMapping("/performance/start")
    public ResponseEntity<Map<String, Object>> startPerformanceTest(@RequestParam(defaultValue = "100000") int records) {
        Map<String, Object> response = new HashMap<>();
        
        if (performanceTestService.isTestRunning()) {
            response.put("success", false);
            response.put("message", "Performance test is already running");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            performanceTestService.startPerformanceTest(records);
            response.put("success", true);
            response.put("message", "Performance test started with " + records + " records");
            response.put("records", records);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to start performance test: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/performance/status")
    public ResponseEntity<Map<String, Object>> getPerformanceTestStatus() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            response.put("success", true);
            response.put("testRunning", performanceTestService.isTestRunning());
            response.put("insertedCount", performanceTestService.getInsertedCount());
            response.put("totalInserted", performanceTestService.getTotalInserted());
            response.put("totalRecordsInRedis", performanceTestService.getTotalDataCount());
            response.put("performanceTestRecords", performanceTestService.getPerformanceTestDataCount());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to get status: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/performance/count")
    public ResponseEntity<Map<String, Object>> getPerformanceTestCount() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            long totalCount = performanceTestService.getTotalDataCount();
            long perfTestCount = performanceTestService.getPerformanceTestDataCount();
            
            response.put("success", true);
            response.put("totalRecordsInRedis", totalCount);
            response.put("performanceTestRecords", perfTestCount);
            response.put("otherRecords", totalCount - perfTestCount);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to get count: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @DeleteMapping("/performance/clear")
    public ResponseEntity<Map<String, Object>> clearPerformanceTestData() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            performanceTestService.clearPerformanceTestData();
            response.put("success", true);
            response.put("message", "Performance test data cleared successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to clear performance test data: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // Spring Data Redis Endpoints
    @PostMapping("/spring-data/data")
    public ResponseEntity<Map<String, Object>> insertSpringData(@RequestBody TestData testData) {
        try {
            springDataRedisService.saveTestData(testData);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Data inserted successfully using Spring Data");
            response.put("id", testData.getId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to insert data: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/spring-data/data/{id}")
    public ResponseEntity<Map<String, Object>> getSpringData(@PathVariable String id) {
        try {
            var data = springDataRedisService.getTestData(id);
            Map<String, Object> response = new HashMap<>();
            if (data.isPresent()) {
                response.put("success", true);
                response.put("data", data.get());
            } else {
                response.put("success", false);
                response.put("message", "Data not found for id: " + id);
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to retrieve data: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/spring-data/data/all")
    public ResponseEntity<Map<String, Object>> getAllSpringData() {
        try {
            var allData = springDataRedisService.getAllTestData();
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", allData);
            response.put("count", allData.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to retrieve all data: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @DeleteMapping("/spring-data/data/{id}")
    public ResponseEntity<Map<String, Object>> deleteSpringData(@PathVariable String id) {
        try {
            boolean deleted = springDataRedisService.deleteTestData(id);
            Map<String, Object> response = new HashMap<>();
            response.put("success", deleted);
            response.put("message", deleted ? "Data deleted successfully" : "Data not found");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to delete data: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // Spring Data Performance Test Endpoints
    @PostMapping("/spring-data/performance/start")
    public ResponseEntity<Map<String, Object>> startSpringDataPerformanceTest(@RequestParam(defaultValue = "100000") int records) {
        Map<String, Object> response = new HashMap<>();
        
        if (springDataPerformanceTestService.isTestRunning()) {
            response.put("success", false);
            response.put("message", "Spring Data performance test is already running");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            springDataPerformanceTestService.startPerformanceTest(records);
            response.put("success", true);
            response.put("message", "Spring Data performance test started with " + records + " records");
            response.put("records", records);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to start Spring Data performance test: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/spring-data/performance/status")
    public ResponseEntity<Map<String, Object>> getSpringDataPerformanceTestStatus() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            response.put("success", true);
            response.put("testRunning", springDataPerformanceTestService.isTestRunning());
            response.put("insertedCount", springDataPerformanceTestService.getInsertedCount());
            response.put("totalInserted", springDataPerformanceTestService.getTotalInserted());
            response.put("totalRecordsInRedis", springDataPerformanceTestService.getTotalDataCount());
            response.put("performanceTestRecords", springDataPerformanceTestService.getPerformanceTestDataCount());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to get Spring Data status: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/spring-data/performance/count")
    public ResponseEntity<Map<String, Object>> getSpringDataPerformanceTestCount() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            long totalCount = springDataPerformanceTestService.getTotalDataCount();
            long perfTestCount = springDataPerformanceTestService.getPerformanceTestDataCount();
            
            response.put("success", true);
            response.put("totalRecordsInRedis", totalCount);
            response.put("performanceTestRecords", perfTestCount);
            response.put("otherRecords", totalCount - perfTestCount);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to get Spring Data count: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @DeleteMapping("/spring-data/performance/clear")
    public ResponseEntity<Map<String, Object>> clearSpringDataPerformanceTestData() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            springDataPerformanceTestService.clearPerformanceTestData();
            response.put("success", true);
            response.put("message", "Spring Data performance test data cleared successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to clear Spring Data performance test data: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // Redis Comparison Test Endpoints
    @PostMapping("/comparison/start")
    public ResponseEntity<Map<String, Object>> startComparisonTest(@RequestParam(defaultValue = "10000") int records) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (redisComparisonService.isTestRunning()) {
                response.put("success", false);
                response.put("message", "Comparison test is already running");
                return ResponseEntity.badRequest().body(response);
            }
            
            redisComparisonService.startComparisonTest(records);
            response.put("success", true);
            response.put("message", "Redis comparison test started with " + records + " records");
            response.put("records", records);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to start comparison test: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/comparison/status")
    public ResponseEntity<Map<String, Object>> getComparisonTestStatus() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            response.put("success", true);
            response.put("testRunning", redisComparisonService.isTestRunning());
            response.put("redisTemplateInserted", redisComparisonService.getRedisTemplateInsertedCount());
            response.put("springDataInserted", redisComparisonService.getSpringDataInsertedCount());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to get comparison test status: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @DeleteMapping("/comparison/clear")
    public ResponseEntity<Map<String, Object>> clearComparisonTestData() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            redisComparisonService.clearAllTestData();
            response.put("success", true);
            response.put("message", "All comparison test data cleared successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to clear comparison test data: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
