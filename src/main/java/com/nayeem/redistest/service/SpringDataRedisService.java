package com.nayeem.redistest.service;

import com.nayeem.redistest.model.TestData;
import com.nayeem.redistest.repository.TestDataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SpringDataRedisService {

    private final TestDataRepository testDataRepository;

    public void saveTestData(TestData testData) {
        testData.setCreatedAt(LocalDateTime.now());
        testDataRepository.save(testData);
    }

    public Optional<TestData> getTestData(String id) {
        return testDataRepository.findById(id);
    }

    public boolean deleteTestData(String id) {
        if (testDataRepository.existsById(id)) {
            testDataRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public boolean existsTestData(String id) {
        return testDataRepository.existsById(id);
    }

    public List<TestData> getAllTestData() {
        return (List<TestData>) testDataRepository.findAll();
    }

    public long getTotalDataCount() {
        return testDataRepository.count();
    }

    public List<TestData> getPerformanceTestData() {
        return testDataRepository.findByNameStartingWith("Performance Test Data");
    }

    public long getPerformanceTestDataCount() {
        return testDataRepository.countByNameStartingWith("Performance Test Data");
    }

    public void clearPerformanceTestData() {
        List<TestData> perfTestData = testDataRepository.findByNameStartingWith("Performance Test Data");
        if (!perfTestData.isEmpty()) {
            testDataRepository.deleteAll(perfTestData);
            log.info("Cleared {} performance test records", perfTestData.size());
        }
    }
}
