package com.mediq.service;

import com.mediq.dto.*;

import java.util.List;

public interface PharmacyService {

    MedicineResponse addMedicine(AddMedicineRequest request);

    MedicineResponse updateStock(Long medicineId, UpdateStockRequest request);

    MedicineResponse getMedicineById(Long medicineId);

    List<MedicineResponse> getAllMedicines();

    List<MedicineResponse> searchMedicines(String keyword);

    List<MedicineResponse> getLowStockAlerts();

    List<DispenseRecordResponse> dispenseMedicines(DispenseMedicineRequest request);

    List<DispenseRecordResponse> getDispenseRecordsForPrescription(Long prescriptionId);

    List<DispenseRecordResponse> getPharmacistDispenseHistory();
}
