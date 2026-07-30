package com.mediq.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueueDashboardResponse {

    private Long campId;
    private String campTitle;
    private long totalTokens;
    private long waitingCount;
    private long inVitalsCount;
    private long waitingForDoctorCount;
    private long inConsultationCount;
    private long pharmacyCount;
    private long referredCount;
    private long completedCount;
    private int estimatedAverageWaitMinutes;
}
