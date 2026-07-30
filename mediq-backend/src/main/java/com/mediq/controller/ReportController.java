package com.mediq.controller;

import com.mediq.dto.*;
import com.mediq.service.PatientService;
import com.mediq.service.PharmacyService;
import com.mediq.service.ReportService;
import com.mediq.util.ExcelGeneratorUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@Tag(name = "Reports & Analytics Module", description = "APIs for Camp Analytics, Doctor Workload Reports, Medicine Consumption Reports, Referral Analytics, and CSV Export")
public class ReportController {

    private final ReportService reportService;
    private final PatientService patientService;
    private final PharmacyService pharmacyService;
    private final ExcelGeneratorUtil excelGeneratorUtil;

    @GetMapping("/camp/{campId}")
    @Operation(summary = "Get Camp Analytics Summary", description = "Aggregates total registered patients, vitals, consultations, prescriptions, referrals, and queue completion rate.")
    public ResponseEntity<ApiResponse<CampAnalyticsResponse>> getCampAnalytics(@PathVariable Long campId) {
        CampAnalyticsResponse analytics = reportService.getCampAnalytics(campId);
        return ResponseEntity.ok(ApiResponse.success(analytics, "Camp analytics fetched successfully"));
    }

    @GetMapping("/doctors/{campId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get Doctor Workload Report", description = "Retrieves consultation counts, prescriptions issued, and referrals generated per doctor.")
    public ResponseEntity<ApiResponse<List<DoctorReportResponse>>> getDoctorWorkloadReport(@PathVariable Long campId) {
        List<DoctorReportResponse> responses = reportService.getDoctorWorkloadReport(campId);
        return ResponseEntity.ok(ApiResponse.success(responses, "Doctor workload report fetched successfully"));
    }

    @GetMapping("/medicines")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get Medicine Consumption & Stock Report", description = "Retrieves medicine stock levels, alert thresholds, and consumption status.")
    public ResponseEntity<ApiResponse<List<MedicineReportResponse>>> getMedicineReport() {
        List<MedicineReportResponse> responses = reportService.getMedicineConsumptionReport();
        return ResponseEntity.ok(ApiResponse.success(responses, "Medicine report fetched successfully"));
    }

    @GetMapping("/referrals/{campId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get Referral Analytics Report", description = "Retrieves referral status breakdowns (CREATED, SENT, VISITED, UNDER_TREATMENT, COMPLETED) and critical case counts.")
    public ResponseEntity<ApiResponse<ReferralReportResponse>> getReferralReport(@PathVariable Long campId) {
        ReferralReportResponse report = reportService.getReferralReport(campId);
        return ResponseEntity.ok(ApiResponse.success(report, "Referral report fetched successfully"));
    }

    @GetMapping("/export/patients/csv")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Export Patients Report as CSV", description = "Generates and downloads CSV report of registered patients.")
    public ResponseEntity<byte[]> exportPatientsCsv(@RequestParam(required = false) Long campId) {
        List<PatientResponse> patients = patientService.searchPatients(campId, null, 0, 1000, "id", "asc").getContent();
        byte[] csvBytes = excelGeneratorUtil.generatePatientCsvReport(patients);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment", "Patients-Report.csv");

        return new ResponseEntity<>(csvBytes, headers, HttpStatus.OK);
    }

    @GetMapping("/export/medicines/csv")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Export Medicine Inventory Report as CSV", description = "Generates and downloads CSV report of medicine inventory.")
    public ResponseEntity<byte[]> exportMedicinesCsv() {
        List<MedicineResponse> medicines = pharmacyService.getAllMedicines();
        byte[] csvBytes = excelGeneratorUtil.generateMedicineCsvReport(medicines);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment", "Medicine-Inventory-Report.csv");

        return new ResponseEntity<>(csvBytes, headers, HttpStatus.OK);
    }
}
