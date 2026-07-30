package com.mediq.dto;

import com.mediq.constants.CampStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCampRequest {

    private String campCode;

    @NotBlank(message = "Camp title is required")
    private String title;

    private String description;

    @NotBlank(message = "Location is required")
    private String location;

    private String venue;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;

    private String startTime;
    private String endTime;
    private String operatingHours;

    private Integer targetCapacity;

    private CampStatus status;

    private Set<Long> doctorIds;
    private Set<Long> nurseIds;
    private Set<Long> volunteerIds;
}
