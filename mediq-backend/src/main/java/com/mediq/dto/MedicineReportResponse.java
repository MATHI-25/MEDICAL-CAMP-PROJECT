package com.mediq.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicineReportResponse {

    private Long medicineId;
    private String medicineCode;
    private String name;
    private String category;
    private int currentStock;
    private int minAlertQuantity;
    private boolean isLowStock;
}
