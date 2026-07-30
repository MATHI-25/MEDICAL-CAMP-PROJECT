package com.mediq.controller;

import com.mediq.constants.CampStatus;
import com.mediq.dto.ApiResponse;
import com.mediq.dto.AssignStaffRequest;
import com.mediq.dto.CampResponse;
import com.mediq.dto.CreateCampRequest;
import com.mediq.dto.UpdateCampRequest;
import com.mediq.service.CampService;
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
@RequestMapping("/api/v1/camps")
@RequiredArgsConstructor
@Tag(name = "Camp Management", description = "APIs for Medical Camp creation, scheduling, capacity, status lifecycle, and staff assignments")
public class CampController {

    private final CampService campService;

    @PostMapping
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('ORGANIZER')")
    @Operation(summary = "Create a new Medical Camp", description = "Creates a new camp venue schedule with target capacity and initial staff assignments.")
    public ResponseEntity<ApiResponse<CampResponse>> createCamp(@Valid @RequestBody CreateCampRequest request) {
        CampResponse response = campService.createCamp(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Medical camp created successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('ORGANIZER')")
    @Operation(summary = "Update Medical Camp details", description = "Updates title, description, venue, dates, capacity, or status of an existing camp.")
    public ResponseEntity<ApiResponse<CampResponse>> updateCamp(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCampRequest request) {
        CampResponse response = campService.updateCamp(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Medical camp updated successfully"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get camp by ID", description = "Retrieves medical camp details by primary database ID including assigned doctors, nurses, and volunteers.")
    public ResponseEntity<ApiResponse<CampResponse>> getCampById(@PathVariable Long id) {
        CampResponse response = campService.getCampById(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Medical camp fetched successfully"));
    }

    @GetMapping("/code/{campCode}")
    @Operation(summary = "Get camp by unique Camp Code", description = "Retrieves medical camp details by unique camp code (e.g. CAMP-2026-001).")
    public ResponseEntity<ApiResponse<CampResponse>> getCampByCode(@PathVariable String campCode) {
        CampResponse response = campService.getCampByCode(campCode);
        return ResponseEntity.ok(ApiResponse.success(response, "Medical camp fetched successfully"));
    }

    @PostMapping("/{id}/staff")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('ORGANIZER')")
    @Operation(summary = "Assign Doctors, Nurses, and Volunteers to Camp", description = "Updates staff rosters assigned to an active medical camp.")
    public ResponseEntity<ApiResponse<CampResponse>> assignStaff(
            @PathVariable Long id,
            @RequestBody AssignStaffRequest request) {
        CampResponse response = campService.assignStaff(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Camp staff assigned successfully"));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('ORGANIZER')")
    @Operation(summary = "Update Camp Status", description = "Transitions camp status between UPCOMING, ONGOING, COMPLETED, or CANCELLED.")
    public ResponseEntity<ApiResponse<CampResponse>> updateCampStatus(
            @PathVariable Long id,
            @RequestParam CampStatus status) {
        CampResponse response = campService.updateCampStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success(response, "Camp status updated successfully"));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get camps by status", description = "Retrieves list of active camps filtered by status ordered by start date.")
    public ResponseEntity<ApiResponse<List<CampResponse>>> getCampsByStatus(@PathVariable CampStatus status) {
        List<CampResponse> responses = campService.getCampsByStatus(status);
        return ResponseEntity.ok(ApiResponse.success(responses, "Medical camps fetched successfully"));
    }

    @GetMapping
    @Operation(summary = "Search Medical Camps", description = "Search camps by status, title/location keyword with pagination.")
    public ResponseEntity<ApiResponse<Page<CampResponse>>> searchCamps(
            @RequestParam(required = false) CampStatus status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "startDate") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        Page<CampResponse> responses = campService.searchCamps(status, keyword, page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success(responses, "Medical camps searched successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('ORGANIZER')")
    @Operation(summary = "Soft delete medical camp", description = "Marks camp as deleted in system records.")
    public ResponseEntity<ApiResponse<Void>> deleteCamp(@PathVariable Long id) {
        campService.deleteCamp(id);
        return ResponseEntity.ok(ApiResponse.success("Medical camp deleted successfully"));
    }
}
