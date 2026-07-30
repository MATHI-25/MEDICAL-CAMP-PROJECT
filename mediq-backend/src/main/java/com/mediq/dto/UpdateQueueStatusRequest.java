package com.mediq.dto;

import com.mediq.constants.QueueStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateQueueStatusRequest {

    @NotNull(message = "Queue status is required")
    private QueueStatus status;

    private Long assignedDoctorId;
}
