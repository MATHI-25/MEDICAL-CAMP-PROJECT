package com.mediq.controller;

import com.mediq.dto.ApiResponse;
import com.mediq.dto.GlobalSearchResultResponse;
import com.mediq.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
@Tag(name = "Master Search Engine", description = "Global multi-entity search endpoint querying Patients, Staff Users, Camps, Prescriptions, Referrals, and Medicines")
public class SearchController {

    private final ReportService reportService;

    @GetMapping
    @Operation(summary = "Unified Master Search", description = "Executes a global search across Patient ID, Member ID, Phone Number, Camp Code, Doctor Name, Referral ID, Prescription ID, or Medicine Name.")
    public ResponseEntity<ApiResponse<GlobalSearchResultResponse>> globalSearch(@RequestParam String query) {
        GlobalSearchResultResponse results = reportService.globalSearch(query);
        return ResponseEntity.ok(ApiResponse.success(results, "Global search executed successfully"));
    }
}
