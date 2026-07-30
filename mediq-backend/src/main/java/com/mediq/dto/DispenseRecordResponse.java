package com.mediq.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DispenseRecordResponse {

    private Long id;
    private Long prescriptionId;
    private String prescriptionCode;
    private Long prescriptionItemId;
    private String medicineName;
    private String medicineCode;
    private UserResponse pharmacist;
    private Integer quantityDispensed;
    private LocalDateTime dispenseDate;
    private String remarks;
}
