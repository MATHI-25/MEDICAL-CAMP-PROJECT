package com.mediq.controller;

import com.mediq.dto.ApiResponse;
import com.mediq.dto.PatientVitalsResponse;
import com.mediq.dto.RecordVitalsRequest;
import com.mediq.service.NurseService;
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
@RequestMapping("/api/v1/nurse")
@RequiredArgsConstructor
@Tag(name = "Nurse Vitals Module", description = "APIs for clinical vitals recording (BP, Pulse, Temp, SpO2, Blood Sugar, Pain Scale, BMI), forwarding to Doctor, and vitals history")
public class NurseController {

    private final NurseService nurseService;

    @PostMapping("/vitals")
    @PreAuthorize("hasRole('NURSE') or hasRole('ORGANIZER') or hasRole('SYSTEM_ADMIN')")
    @Operation(summary = "Record patient clinical vitals", description = "Records physical measurements, symptoms, pain scale, calculates BMI, and forwards queue token to doctor consultation queue.")
    public ResponseEntity<ApiResponse<PatientVitalsResponse>> recordVitals(@Valid @RequestBody RecordVitalsRequest request) {
        PatientVitalsResponse response = nurseService.recordVitals(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Patient vitals recorded and forwarded to doctor queue successfully"));
    }

    @GetMapping("/vitals/{id}")
    @Operation(summary = "Get vitals record by ID", description = "Retrieves vitals measurements by database ID.")
    public ResponseEntity<ApiResponse<PatientVitalsResponse>> getVitalsById(@PathVariable Long id) {
        PatientVitalsResponse response = nurseService.getVitalsById(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Patient vitals fetched successfully"));
    }

    @GetMapping("/vitals/token/{tokenId}")
    @Operation(summary = "Get vitals record by Queue Token ID", description = "Retrieves vitals recorded for a specific queue token.")
    public ResponseEntity<ApiResponse<PatientVitalsResponse>> getVitalsByTokenId(@PathVariable Long tokenId) {
        PatientVitalsResponse response = nurseService.getVitalsByTokenId(tokenId);
        return ResponseEntity.ok(ApiResponse.success(response, "Patient vitals fetched successfully"));
    }

    @GetMapping("/vitals/patient/{patientId}")
    @Operation(summary = "Get patient vitals history", description = "Retrieves historical vitals measurements for a patient ordered by date.")
    public ResponseEntity<ApiResponse<List<PatientVitalsResponse>>> getPatientVitalsHistory(@PathVariable Long patientId) {
        List<PatientVitalsResponse> responses = nurseService.getPatientVitalsHistory(patientId);
        return ResponseEntity.ok(ApiResponse.success(responses, "Patient vitals history fetched successfully"));
    }
}
