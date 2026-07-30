package com.mediq.mapper;

import com.mediq.dto.CampResponse;
import com.mediq.entity.MedicalCamp;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CampMapper {

    private final UserMapper userMapper;

    public CampResponse toResponse(MedicalCamp camp) {
        if (camp == null) {
            return null;
        }

        return CampResponse.builder()
                .id(camp.getId())
                .campCode(camp.getCampCode())
                .title(camp.getTitle())
                .description(camp.getDescription())
                .location(camp.getLocation())
                .venue(camp.getVenue())
                .startDate(camp.getStartDate())
                .endDate(camp.getEndDate())
                .startTime(camp.getStartTime())
                .endTime(camp.getEndTime())
                .operatingHours(camp.getOperatingHours())
                .targetCapacity(camp.getTargetCapacity())
                .status(camp.getStatus())
                .assignedDoctors(camp.getAssignedDoctors() != null ?
                        camp.getAssignedDoctors().stream().map(userMapper::toResponse).collect(Collectors.toList()) : Collections.emptyList())
                .assignedNurses(camp.getAssignedNurses() != null ?
                        camp.getAssignedNurses().stream().map(userMapper::toResponse).collect(Collectors.toList()) : Collections.emptyList())
                .assignedVolunteers(camp.getAssignedVolunteers() != null ?
                        camp.getAssignedVolunteers().stream().map(userMapper::toResponse).collect(Collectors.toList()) : Collections.emptyList())
                .createdAt(camp.getCreatedAt())
                .updatedAt(camp.getUpdatedAt())
                .build();
    }
}
