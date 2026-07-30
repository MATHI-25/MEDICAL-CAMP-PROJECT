package com.mediq.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddMedicineRequest {

    @NotBlank(message = "Medicine code is required")
    private String medicineCode;

    @NotBlank(message = "Medicine name is required")
    private String name;

    private String category;

    @NotBlank(message = "Batch number is required")
    private String batchNumber;

    private String manufacturer;

    @NotNull(message = "Expiry date is required")
    @Future(message = "Expiry date must be in the future")
    private LocalDate expiryDate;

    @NotNull(message = "Stock quantity is required")
    @Min(value = 0, message = "Stock quantity cannot be negative")
    private Integer stockQuantity;

    private Integer minAlertQuantity;

    private Double unitPrice;
}
