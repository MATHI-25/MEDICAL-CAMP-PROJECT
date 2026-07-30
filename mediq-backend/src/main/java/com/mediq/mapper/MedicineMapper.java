package com.mediq.mapper;

import com.mediq.dto.MedicineResponse;
import com.mediq.entity.MedicineInventory;
import org.springframework.stereotype.Component;

@Component
public class MedicineMapper {

    public MedicineResponse toResponse(MedicineInventory medicine) {
        if (medicine == null) {
            return null;
        }

        boolean isLow = medicine.getStockQuantity() != null &&
                        medicine.getMinAlertQuantity() != null &&
                        medicine.getStockQuantity() <= medicine.getMinAlertQuantity();

        return MedicineResponse.builder()
                .id(medicine.getId())
                .medicineCode(medicine.getMedicineCode())
                .name(medicine.getName())
                .category(medicine.getCategory())
                .batchNumber(medicine.getBatchNumber())
                .manufacturer(medicine.getManufacturer())
                .expiryDate(medicine.getExpiryDate())
                .stockQuantity(medicine.getStockQuantity())
                .minAlertQuantity(medicine.getMinAlertQuantity())
                .unitPrice(medicine.getUnitPrice())
                .isLowStock(isLow)
                .createdAt(medicine.getCreatedAt())
                .build();
    }
}
