package com.mediq.dto;

import com.mediq.constants.CampStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCampRequest {

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

    private Integer targetCapacity;

    @NotNull(message = "Camp status is required")
    private CampStatus status;
}
