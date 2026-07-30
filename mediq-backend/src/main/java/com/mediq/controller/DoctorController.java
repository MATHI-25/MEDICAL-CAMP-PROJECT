package com.mediq.controller;

import com.mediq.dto.ApiResponse;
import com.mediq.dto.ConsultationResponse;
import com.mediq.dto.CreateConsultationRequest;
import com.mediq.service.DoctorService;
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
@RequestMapping("/api/v1/doctor")
@RequiredArgsConstructor
@Tag(name = "Doctor Consultation Module", description = "APIs for Doctor Consultations, Medical Diagnosis, Disease Identification, Lab Test Recommendations, Clinical Decision Branching, and Consultation History")
public class DoctorController {

    private final DoctorService doctorService;

    @PostMapping("/consultations")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('SYSTEM_ADMIN')")
    @Operation(summary = "Save doctor consultation and diagnosis", description = "Records medical diagnosis, disease identification, lab recommendations, notes, and updates queue token (SENT_TO_PHARMACY or REFERRED_TO_HOSPITAL).")
    public ResponseEntity<ApiResponse<ConsultationResponse>> saveConsultation(@Valid @RequestBody CreateConsultationRequest request) {
        ConsultationResponse response = doctorService.saveConsultation(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Doctor consultation recorded successfully"));
    }

    @GetMapping("/consultations/{id}")
    @Operation(summary = "Get consultation by ID", description = "Retrieves consultation record by database ID.")
    public ResponseEntity<ApiResponse<ConsultationResponse>> getConsultationById(@PathVariable Long id) {
        ConsultationResponse response = doctorService.getConsultationById(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Consultation fetched successfully"));
    }

    @GetMapping("/consultations/code/{consultationCode}")
    @Operation(summary = "Get consultation by unique Consultation Code", description = "Retrieves consultation details by code (e.g. CNS-2026-0001).")
    public ResponseEntity<ApiResponse<ConsultationResponse>> getConsultationByCode(@PathVariable String consultationCode) {
        ConsultationResponse response = doctorService.getConsultationByCode(consultationCode);
        return ResponseEntity.ok(ApiResponse.success(response, "Consultation fetched successfully"));
    }

    @GetMapping("/consultations/patient/{patientId}")
    @Operation(summary = "Get patient consultation history", description = "Retrieves historical consultations for a patient ordered by date.")
    public ResponseEntity<ApiResponse<List<ConsultationResponse>>> getPatientConsultationHistory(@PathVariable Long patientId) {
        List<ConsultationResponse> responses = doctorService.getPatientConsultationHistory(patientId);
        return ResponseEntity.ok(ApiResponse.success(responses, "Patient consultation history fetched successfully"));
    }

    @GetMapping("/consultations/camp/{campId}")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ORGANIZER') or hasRole('SYSTEM_ADMIN')")
    @Operation(summary = "Get consultations for camp", description = "Retrieves consultations performed by doctor at specific camp.")
    public ResponseEntity<ApiResponse<List<ConsultationResponse>>> getDoctorConsultations(
            @PathVariable Long campId,
            @RequestParam Long doctorId) {
        List<ConsultationResponse> responses = doctorService.getDoctorConsultations(doctorId, campId);
        return ResponseEntity.ok(ApiResponse.success(responses, "Doctor consultations fetched successfully"));
    }
}
