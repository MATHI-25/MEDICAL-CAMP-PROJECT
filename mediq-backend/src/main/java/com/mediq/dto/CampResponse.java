package com.mediq.dto;

import com.mediq.constants.CampStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampResponse {

    private Long id;
    private String campCode;
    private String title;
    private String description;
    private String location;
    private String venue;
    private LocalDate startDate;
    private LocalDate endDate;
    private String startTime;
    private String endTime;
    private String operatingHours;
    private Integer targetCapacity;
    private CampStatus status;
    private List<UserResponse> assignedDoctors;
    private List<UserResponse> assignedNurses;
    private List<UserResponse> assignedVolunteers;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
