package com.mediq.controller;

import com.mediq.constants.ReferralStatus;
import com.mediq.dto.ApiResponse;
import com.mediq.dto.CreateReferralRequest;
import com.mediq.dto.ReferralResponse;
import com.mediq.dto.UpdateReferralStatusRequest;
import com.mediq.service.ReferralService;
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
@RequestMapping("/api/v1/referrals")
@RequiredArgsConstructor
@Tag(name = "Hospital Referral Module", description = "CORE FEATURE: APIs for Hospital Referrals, Destination Hospitals, Departments, Urgency Levels, Referral Status Lifecycle (CREATED -> SENT -> VISITED -> UNDER_TREATMENT -> COMPLETED), and Printable Referral Letter PDF")
public class ReferralController {

    private final ReferralService referralService;

    @PostMapping
    @PreAuthorize("hasRole('DOCTOR') or hasRole('SYSTEM_ADMIN')")
    @Operation(summary = "Create Digital Hospital Referral", description = "Issues a digital referral when patient treatment cannot be completed inside camp. Generates unique Referral ID (REF-YYYY-XXXX).")
    public ResponseEntity<ApiResponse<ReferralResponse>> createReferral(@Valid @RequestBody CreateReferralRequest request) {
        ReferralResponse response = referralService.createReferral(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Hospital referral issued successfully"));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ORGANIZER') or hasRole('SYSTEM_ADMIN')")
    @Operation(summary = "Update Referral Status", description = "Transitions referral status (CREATED, SENT, VISITED, UNDER_TREATMENT, COMPLETED, CANCELLED).")
    public ResponseEntity<ApiResponse<ReferralResponse>> updateReferralStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateReferralStatusRequest request) {
        ReferralResponse response = referralService.updateReferralStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Referral status updated successfully"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get referral by ID", description = "Retrieves referral record by database ID.")
    public ResponseEntity<ApiResponse<ReferralResponse>> getReferralById(@PathVariable Long id) {
        ReferralResponse response = referralService.getReferralById(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Referral fetched successfully"));
    }

    @GetMapping("/code/{referralId}")
    @Operation(summary = "Get referral by unique Referral ID", description = "Retrieves referral details by code (e.g. REF-2026-0001).")
    public ResponseEntity<ApiResponse<ReferralResponse>> getReferralByCode(@PathVariable String referralId) {
        ReferralResponse response = referralService.getReferralByCode(referralId);
        return ResponseEntity.ok(ApiResponse.success(response, "Referral fetched successfully"));
    }

    @GetMapping("/consultation/{consultationId}")
    @Operation(summary = "Get referral by Consultation ID", description = "Retrieves referral linked to a specific doctor consultation.")
    public ResponseEntity<ApiResponse<ReferralResponse>> getReferralByConsultationId(@PathVariable Long consultationId) {
        ReferralResponse response = referralService.getReferralByConsultationId(consultationId);
        return ResponseEntity.ok(ApiResponse.success(response, "Referral fetched successfully"));
    }

    @GetMapping("/patient/{patientId}")
    @Operation(summary = "Get patient referral history", description = "Retrieves historical hospital referrals for a patient.")
    public ResponseEntity<ApiResponse<List<ReferralResponse>>> getPatientReferrals(@PathVariable Long patientId) {
        List<ReferralResponse> responses = referralService.getPatientReferrals(patientId);
        return ResponseEntity.ok(ApiResponse.success(responses, "Patient referrals fetched successfully"));
    }

    @GetMapping("/status/{campId}")
    @Operation(summary = "Get referrals by status for camp", description = "Retrieves referrals for a camp filtered by status.")
    public ResponseEntity<ApiResponse<List<ReferralResponse>>> getReferralsByStatus(
            @PathVariable Long campId,
            @RequestParam ReferralStatus status) {
        List<ReferralResponse> responses = referralService.getReferralsByStatus(campId, status);
        return ResponseEntity.ok(ApiResponse.success(responses, "Referrals fetched successfully"));
    }

    @GetMapping("/{id}/pdf")
    @Operation(summary = "Generate and download Referral Letter PDF", description = "Generates an official OpenPDF Referral Letter document byte stream formatted for printing.")
    public ResponseEntity<byte[]> downloadReferralPdf(@PathVariable Long id) {
        byte[] pdfBytes = referralService.generateReferralPdf(id);
        ReferralResponse referral = referralService.getReferralById(id);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("inline", "Referral-Letter-" + referral.getReferralId() + ".pdf");
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }
}
