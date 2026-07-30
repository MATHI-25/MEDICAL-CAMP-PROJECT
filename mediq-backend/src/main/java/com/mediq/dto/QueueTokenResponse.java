package com.mediq.dto;

import com.mediq.constants.QueueStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueueTokenResponse {

    private Long id;
    private String tokenNumber;
    private Integer sequenceNumber;
    private PatientResponse patient;
    private Long campId;
    private String campTitle;
    private UserResponse assignedDoctor;
    private QueueStatus status;
    private Integer estimatedWaitMinutes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
