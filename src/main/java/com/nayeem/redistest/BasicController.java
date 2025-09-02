package com.nayeem.redistest;

import com.nayeem.redistest.model.TestData;
import com.nayeem.redistest.service.RedisService;
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
}
