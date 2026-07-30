package com.mediq.mapper;

import com.mediq.dto.DispenseRecordResponse;
import com.mediq.entity.DispenseRecord;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DispenseMapper {

    private final UserMapper userMapper;

    public DispenseRecordResponse toResponse(DispenseRecord record) {
        if (record == null) {
            return null;
        }

        return DispenseRecordResponse.builder()
                .id(record.getId())
                .prescriptionId(record.getPrescription() != null ? record.getPrescription().getId() : null)
                .prescriptionCode(record.getPrescription() != null ? record.getPrescription().getPrescriptionCode() : null)
                .prescriptionItemId(record.getPrescriptionItem() != null ? record.getPrescriptionItem().getId() : null)
                .medicineName(record.getPrescriptionItem() != null ? record.getPrescriptionItem().getMedicineName() : null)
                .medicineCode(record.getMedicine() != null ? record.getMedicine().getMedicineCode() : null)
                .pharmacist(userMapper.toResponse(record.getPharmacist()))
                .quantityDispensed(record.getQuantityDispensed())
                .dispenseDate(record.getDispenseDate())
                .remarks(record.getRemarks())
                .build();
    }
}
