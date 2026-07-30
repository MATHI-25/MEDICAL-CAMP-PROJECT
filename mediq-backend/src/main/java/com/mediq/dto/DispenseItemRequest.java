package com.mediq.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DispenseItemRequest {

    @NotNull(message = "Prescription Item ID is required")
    private Long prescriptionItemId;

    private Long medicineId;

    @NotNull(message = "Quantity to dispense is required")
    @Min(value = 1, message = "Dispense quantity must be at least 1")
    private Integer quantityToDispense;

    private String remarks;
}
