package com.mediq.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DispenseMedicineRequest {

    @NotNull(message = "Prescription ID is required")
    private Long prescriptionId;

    @NotEmpty(message = "Dispense items list cannot be empty")
    @Valid
    private List<DispenseItemRequest> items;

    private String remarks;
}
