package com.nayeem.redistest.repository;

import com.nayeem.redistest.model.TestData;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestDataRepository extends CrudRepository<TestData, String> {
    
    // Find all records
    List<TestData> findAll();
    
    // Find by name (using indexed field)
    List<TestData> findByName(String name);
    
    // Find by name containing (using indexed field)
    List<TestData> findByNameContaining(String name);
    
    // Find by performance test prefix
    List<TestData> findByNameStartingWith(String prefix);
    
    // Count all records
    long count();
    
    // Count by name starting with
    long countByNameStartingWith(String prefix);
}
