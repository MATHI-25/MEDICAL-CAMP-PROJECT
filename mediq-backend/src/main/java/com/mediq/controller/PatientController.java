package com.mediq.controller;

import com.mediq.dto.*;
import com.mediq.service.MedicalHistoryService;
import com.mediq.service.PatientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/patients")
@RequiredArgsConstructor
@Tag(name = "Patient Registration & Intake", description = "APIs for Patient Intake, Auto-generated Patient ID (PAT-YYYY-XXXX), Medical History Ledger, and Search")
public class PatientController {

    private final PatientService patientService;
    private final MedicalHistoryService medicalHistoryService;

    @PostMapping
    @PreAuthorize("hasRole('REGISTRATION_VOLUNTEER') or hasRole('NURSE') or hasRole('ORGANIZER') or hasRole('SYSTEM_ADMIN')")
    @Operation(summary = "Register a new patient", description = "Registers patient demographics, emergency contacts, allergies, and chronic conditions. Generates unique Patient ID and initializes timeline ledger.")
    public ResponseEntity<ApiResponse<PatientResponse>> registerPatient(@Valid @RequestBody RegisterPatientRequest request) {
        PatientResponse response = patientService.registerPatient(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Patient registered successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('REGISTRATION_VOLUNTEER') or hasRole('NURSE') or hasRole('DOCTOR') or hasRole('SYSTEM_ADMIN')")
    @Operation(summary = "Update patient demographics and medical info", description = "Updates patient personal info, blood group, allergies, or chronic conditions.")
    public ResponseEntity<ApiResponse<PatientResponse>> updatePatient(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePatientRequest request) {
        PatientResponse response = patientService.updatePatient(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Patient updated successfully"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get patient by database ID", description = "Retrieves complete patient profile by primary database ID.")
    public ResponseEntity<ApiResponse<PatientResponse>> getPatientById(@PathVariable Long id) {
        PatientResponse response = patientService.getPatientById(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Patient fetched successfully"));
    }

    @GetMapping("/patient-id/{patientId}")
    @Operation(summary = "Get patient by unique Patient ID", description = "Retrieves patient record by system Patient ID (e.g. PAT-2026-0001).")
    public ResponseEntity<ApiResponse<PatientResponse>> getPatientByPatientId(@PathVariable String patientId) {
        PatientResponse response = patientService.getPatientByPatientId(patientId);
        return ResponseEntity.ok(ApiResponse.success(response, "Patient fetched successfully"));
    }

    @GetMapping("/phone/{phone}")
    @Operation(summary = "Get patients by phone number", description = "Retrieves all patient records matching phone number.")
    public ResponseEntity<ApiResponse<List<PatientResponse>>> getPatientsByPhone(@PathVariable String phone) {
        List<PatientResponse> responses = patientService.getPatientsByPhone(phone);
        return ResponseEntity.ok(ApiResponse.success(responses, "Patients fetched successfully"));
    }

    @GetMapping
    @Operation(summary = "Search registered patients", description = "Search patients by camp ID, name, patient ID, or phone number with pagination.")
    public ResponseEntity<ApiResponse<Page<PatientResponse>>> searchPatients(
            @RequestParam(required = false) Long campId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Page<PatientResponse> responses = patientService.searchPatients(campId, keyword, page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success(responses, "Patients searched successfully"));
    }

    @GetMapping("/{id}/timeline")
    @Operation(summary = "Get patient medical history timeline", description = "Retrieves complete chronological event ledger (Registration, Vitals, Consultations, Prescriptions, Referrals).")
    public ResponseEntity<ApiResponse<List<MedicalHistoryResponse>>> getPatientTimeline(@PathVariable Long id) {
        List<MedicalHistoryResponse> timeline = medicalHistoryService.getPatientTimeline(id);
        return ResponseEntity.ok(ApiResponse.success(timeline, "Patient medical timeline fetched successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('ORGANIZER')")
    @Operation(summary = "Soft delete patient record", description = "Marks patient record as deleted in system database.")
    public ResponseEntity<ApiResponse<Void>> deletePatient(@PathVariable Long id) {
        patientService.deletePatient(id);
        return ResponseEntity.ok(ApiResponse.success("Patient record deleted successfully"));
    }
}
