package com.mediq.controller;

import com.mediq.constants.QueueStatus;
import com.mediq.dto.*;
import com.mediq.service.QueueService;
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
@RequestMapping("/api/v1/queue")
@RequiredArgsConstructor
@Tag(name = "Queue Engine", description = "Real-time Queue Token Generation, Wait-Time Estimation, Live Nurse Queue, Live Doctor Queue, and Camp Monitor Dashboard")
public class QueueController {

    private final QueueService queueService;

    @PostMapping("/generate")
    @PreAuthorize("hasRole('REGISTRATION_VOLUNTEER') or hasRole('NURSE') or hasRole('ORGANIZER') or hasRole('SYSTEM_ADMIN')")
    @Operation(summary = "Generate Queue Token for Patient", description = "Issues a daily sequential token (e.g. TKN-001) for a registered patient and calculates estimated wait time.")
    public ResponseEntity<ApiResponse<QueueTokenResponse>> generateToken(@Valid @RequestBody GenerateTokenRequest request) {
        QueueTokenResponse response = queueService.generateToken(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Queue token generated successfully"));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update Queue Token Status", description = "Transitions queue token lifecycle (WAITING -> IN_VITALS -> WAITING_FOR_DOCTOR -> IN_CONSULTATION -> SENT_TO_PHARMACY -> REFERRED_TO_HOSPITAL -> COMPLETED).")
    public ResponseEntity<ApiResponse<QueueTokenResponse>> updateTokenStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateQueueStatusRequest request) {
        QueueTokenResponse response = queueService.updateTokenStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Queue token status updated successfully"));
    }

    @PatchMapping("/{id}/assign-doctor")
    @PreAuthorize("hasRole('NURSE') or hasRole('ORGANIZER') or hasRole('DOCTOR') or hasRole('SYSTEM_ADMIN')")
    @Operation(summary = "Assign Doctor to Queue Token", description = "Routes a patient from Nurse Vitals to a specific Doctor's consultation queue.")
    public ResponseEntity<ApiResponse<QueueTokenResponse>> assignDoctor(
            @PathVariable Long id,
            @RequestParam Long doctorId) {
        QueueTokenResponse response = queueService.assignDoctor(id, doctorId);
        return ResponseEntity.ok(ApiResponse.success(response, "Doctor assigned to queue token successfully"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get Queue Token details by ID", description = "Retrieves token details including patient info, status, and wait time.")
    public ResponseEntity<ApiResponse<QueueTokenResponse>> getTokenById(@PathVariable Long id) {
        QueueTokenResponse response = queueService.getTokenById(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Queue token fetched successfully"));
    }

    @GetMapping("/nurse/{campId}")
    @PreAuthorize("hasRole('NURSE') or hasRole('ORGANIZER') or hasRole('SYSTEM_ADMIN')")
    @Operation(summary = "Get Live Nurse Queue", description = "Retrieves patients waiting for vitals recording at a specific camp.")
    public ResponseEntity<ApiResponse<List<QueueTokenResponse>>> getNurseQueue(@PathVariable Long campId) {
        List<QueueTokenResponse> responses = queueService.getNurseQueue(campId);
        return ResponseEntity.ok(ApiResponse.success(responses, "Nurse queue fetched successfully"));
    }

    @GetMapping("/doctor/{campId}")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ORGANIZER') or hasRole('SYSTEM_ADMIN')")
    @Operation(summary = "Get Live Doctor Queue", description = "Retrieves patients waiting or in consultation for the specified doctor.")
    public ResponseEntity<ApiResponse<List<QueueTokenResponse>>> getDoctorQueue(
            @PathVariable Long campId,
            @RequestParam Long doctorId) {
        List<QueueTokenResponse> responses = queueService.getDoctorQueue(campId, doctorId);
        return ResponseEntity.ok(ApiResponse.success(responses, "Doctor queue fetched successfully"));
    }

    @GetMapping("/status/{campId}")
    @Operation(summary = "Get Queue Tokens by status", description = "Retrieves queue tokens for a camp filtered by queue status.")
    public ResponseEntity<ApiResponse<List<QueueTokenResponse>>> getTokensByStatus(
            @PathVariable Long campId,
            @RequestParam QueueStatus status) {
        List<QueueTokenResponse> responses = queueService.getTokensByStatus(campId, status);
        return ResponseEntity.ok(ApiResponse.success(responses, "Queue tokens fetched successfully"));
    }

    @GetMapping("/dashboard/{campId}")
    @Operation(summary = "Get Live Queue Dashboard Metrics", description = "Retrieves real-time counts for Total, Waiting, Vitals, Consultation, Pharmacy, Referred, and Completed counters.")
    public ResponseEntity<ApiResponse<QueueDashboardResponse>> getQueueDashboard(@PathVariable Long campId) {
        QueueDashboardResponse dashboard = queueService.getQueueDashboard(campId);
        return ResponseEntity.ok(ApiResponse.success(dashboard, "Queue dashboard metrics fetched successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ORGANIZER') or hasRole('SYSTEM_ADMIN') or hasRole('REGISTRATION_VOLUNTEER')")
    @Operation(summary = "Cancel Queue Token", description = "Cancels a queue token if patient leaves or cancels appointment.")
    public ResponseEntity<ApiResponse<Void>> cancelToken(@PathVariable Long id) {
        queueService.cancelToken(id);
        return ResponseEntity.ok(ApiResponse.success("Queue token cancelled successfully"));
    }
}
