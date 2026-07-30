package com.mediq.controller;

import com.mediq.dto.*;
import com.mediq.service.PharmacyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/pharmacy")
@RequiredArgsConstructor
@Tag(name = "Pharmacy & Inventory Module", description = "APIs for Medicine Stock Inventory, Low Stock Alerts, Expiry Warnings, and Digital Prescription Dispensing")
public class PharmacyController {

    private final PharmacyService pharmacyService;

    @PostMapping("/inventory")
    @PreAuthorize("hasRole('PHARMACY') or hasRole('SYSTEM_ADMIN') or hasRole('ORGANIZER')")
    @Operation(summary = "Add medicine item to inventory", description = "Creates a new medicine stock record with batch number, expiry date, stock quantity, and low stock alert threshold.")
    public ResponseEntity<ApiResponse<MedicineResponse>> addMedicine(@Valid @RequestBody AddMedicineRequest request) {
        MedicineResponse response = pharmacyService.addMedicine(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Medicine added to inventory successfully"));
    }

    @PatchMapping("/inventory/{id}/restock")
    @PreAuthorize("hasRole('PHARMACY') or hasRole('SYSTEM_ADMIN') or hasRole('ORGANIZER')")
    @Operation(summary = "Restock medicine inventory quantity", description = "Increases available stock quantity for a medicine item.")
    public ResponseEntity<ApiResponse<MedicineResponse>> updateStock(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStockRequest request) {
        MedicineResponse response = pharmacyService.updateStock(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Medicine stock updated successfully"));
    }

    @GetMapping("/inventory/{id}")
    @Operation(summary = "Get medicine details by ID", description = "Retrieves medicine inventory item by primary database ID.")
    public ResponseEntity<ApiResponse<MedicineResponse>> getMedicineById(@PathVariable Long id) {
        MedicineResponse response = pharmacyService.getMedicineById(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Medicine fetched successfully"));
    }

    @GetMapping("/inventory")
    @Operation(summary = "Get all inventory medicines", description = "Retrieves all active medicine stock items in inventory.")
    public ResponseEntity<ApiResponse<List<MedicineResponse>>> getAllMedicines() {
        List<MedicineResponse> responses = pharmacyService.getAllMedicines();
        return ResponseEntity.ok(ApiResponse.success(responses, "Inventory fetched successfully"));
    }

    @GetMapping("/inventory/search")
    @Operation(summary = "Search inventory medicines", description = "Search medicines by name or category.")
    public ResponseEntity<ApiResponse<List<MedicineResponse>>> searchMedicines(@RequestParam String keyword) {
        List<MedicineResponse> responses = pharmacyService.searchMedicines(keyword);
        return ResponseEntity.ok(ApiResponse.success(responses, "Medicines searched successfully"));
    }

    @GetMapping("/inventory/low-stock")
    @Operation(summary = "Get low stock alert list", description = "Retrieves medicines where stock quantity is below or equal to alert threshold.")
    public ResponseEntity<ApiResponse<List<MedicineResponse>>> getLowStockAlerts() {
        List<MedicineResponse> responses = pharmacyService.getLowStockAlerts();
        return ResponseEntity.ok(ApiResponse.success(responses, "Low stock alerts fetched successfully"));
    }

    @PostMapping("/dispense")
    @PreAuthorize("hasRole('PHARMACY') or hasRole('SYSTEM_ADMIN')")
    @Operation(summary = "Dispense medicines for prescription", description = "Deducts stock from inventory, updates prescription status (PARTIALLY_DISPENSED or DISPENSED), and completes queue token when fully dispensed.")
    public ResponseEntity<ApiResponse<List<DispenseRecordResponse>>> dispenseMedicines(@Valid @RequestBody DispenseMedicineRequest request) {
        List<DispenseRecordResponse> responses = pharmacyService.dispenseMedicines(request);
        return ResponseEntity.ok(ApiResponse.success(responses, "Medicines dispensed successfully"));
    }

    @GetMapping("/dispense/prescription/{prescriptionId}")
    @Operation(summary = "Get dispense history for prescription", description = "Retrieves dispense log entries associated with a prescription.")
    public ResponseEntity<ApiResponse<List<DispenseRecordResponse>>> getDispenseRecordsForPrescription(@PathVariable Long prescriptionId) {
        List<DispenseRecordResponse> responses = pharmacyService.getDispenseRecordsForPrescription(prescriptionId);
        return ResponseEntity.ok(ApiResponse.success(responses, "Dispense records fetched successfully"));
    }

    @GetMapping("/dispense/history")
    @PreAuthorize("hasRole('PHARMACY') or hasRole('SYSTEM_ADMIN')")
    @Operation(summary = "Get pharmacist dispense log history", description = "Retrieves dispensing activity history for the logged in pharmacist.")
    public ResponseEntity<ApiResponse<List<DispenseRecordResponse>>> getPharmacistDispenseHistory() {
        List<DispenseRecordResponse> responses = pharmacyService.getPharmacistDispenseHistory();
        return ResponseEntity.ok(ApiResponse.success(responses, "Pharmacist dispense history fetched successfully"));
    }
}
