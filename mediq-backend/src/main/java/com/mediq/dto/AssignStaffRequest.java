package com.mediq.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignStaffRequest {

    private Set<Long> doctorIds;
    private Set<Long> nurseIds;
    private Set<Long> volunteerIds;
}
