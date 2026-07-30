package com.mediq.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicineResponse {

    private Long id;
    private String medicineCode;
    private String name;
    private String category;
    private String batchNumber;
    private String manufacturer;
    private LocalDate expiryDate;
    private Integer stockQuantity;
    private Integer minAlertQuantity;
    private Double unitPrice;
    private boolean isLowStock;
    private LocalDateTime createdAt;
}
