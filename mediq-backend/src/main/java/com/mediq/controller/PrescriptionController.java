package com.mediq.controller;

import com.mediq.constants.PrescriptionStatus;
import com.mediq.dto.ApiResponse;
import com.mediq.dto.CreatePrescriptionRequest;
import com.mediq.dto.PrescriptionResponse;
import com.mediq.service.PrescriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/prescriptions")
@RequiredArgsConstructor
@Tag(name = "Digital Prescription Engine", description = "APIs for Digital Prescription issuance, medicine list, dosage schedule, professional PDF generation, and print stream")
public class PrescriptionController {

    private final PrescriptionService prescriptionService;

    @PostMapping
    @PreAuthorize("hasRole('DOCTOR') or hasRole('SYSTEM_ADMIN')")
    @Operation(summary = "Create Digital Prescription", description = "Issues a digital prescription with medicine items, dosages, duration, instructions, and doctor signature.")
    public ResponseEntity<ApiResponse<PrescriptionResponse>> createPrescription(@Valid @RequestBody CreatePrescriptionRequest request) {
        PrescriptionResponse response = prescriptionService.createPrescription(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Digital prescription created successfully"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get prescription by ID", description = "Retrieves prescription details by database ID.")
    public ResponseEntity<ApiResponse<PrescriptionResponse>> getPrescriptionById(@PathVariable Long id) {
        PrescriptionResponse response = prescriptionService.getPrescriptionById(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Prescription fetched successfully"));
    }

    @GetMapping("/code/{prescriptionCode}")
    @Operation(summary = "Get prescription by unique Prescription Code", description = "Retrieves prescription details by code (e.g. RX-2026-0001).")
    public ResponseEntity<ApiResponse<PrescriptionResponse>> getPrescriptionByCode(@PathVariable String prescriptionCode) {
        PrescriptionResponse response = prescriptionService.getPrescriptionByCode(prescriptionCode);
        return ResponseEntity.ok(ApiResponse.success(response, "Prescription fetched successfully"));
    }

    @GetMapping("/consultation/{consultationId}")
    @Operation(summary = "Get prescription by Consultation ID", description = "Retrieves prescription linked to a specific doctor consultation.")
    public ResponseEntity<ApiResponse<PrescriptionResponse>> getPrescriptionByConsultationId(@PathVariable Long consultationId) {
        PrescriptionResponse response = prescriptionService.getPrescriptionByConsultationId(consultationId);
        return ResponseEntity.ok(ApiResponse.success(response, "Prescription fetched successfully"));
    }

    @GetMapping("/patient/{patientId}")
    @Operation(summary = "Get patient prescription history", description = "Retrieves historical prescriptions issued to a patient.")
    public ResponseEntity<ApiResponse<List<PrescriptionResponse>>> getPatientPrescriptions(@PathVariable Long patientId) {
        List<PrescriptionResponse> responses = prescriptionService.getPatientPrescriptions(patientId);
        return ResponseEntity.ok(ApiResponse.success(responses, "Patient prescriptions fetched successfully"));
    }

    @GetMapping("/status/{campId}")
    @Operation(summary = "Get prescriptions by status for camp", description = "Retrieves prescriptions for a camp filtered by status (CREATED, DISPENSED, PARTIALLY_DISPENSED).")
    public ResponseEntity<ApiResponse<List<PrescriptionResponse>>> getPrescriptionsByStatus(
            @PathVariable Long campId,
            @RequestParam PrescriptionStatus status) {
        List<PrescriptionResponse> responses = prescriptionService.getPrescriptionsByStatus(campId, status);
        return ResponseEntity.ok(ApiResponse.success(responses, "Prescriptions fetched successfully"));
    }

    @GetMapping("/{id}/pdf")
    @Operation(summary = "Generate and download Digital Prescription PDF", description = "Generates a professional OpenPDF document byte stream formatted for printing or PDF download.")
    public ResponseEntity<byte[]> downloadPrescriptionPdf(@PathVariable Long id) {
        byte[] pdfBytes = prescriptionService.generatePrescriptionPdf(id);
        PrescriptionResponse prescription = prescriptionService.getPrescriptionById(id);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("inline", "Prescription-" + prescription.getPrescriptionCode() + ".pdf");
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }
}
