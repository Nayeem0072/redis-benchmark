package com.nayeem.redistest.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestData {
    private String id;
    private String name;
    private String description;
    private LocalDateTime createdAt;
    private int value;
}
