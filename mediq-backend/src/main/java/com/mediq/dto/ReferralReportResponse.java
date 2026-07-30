package com.mediq.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReferralReportResponse {

    private Long campId;
    private long totalReferrals;
    private long createdCount;
    private long sentCount;
    private long visitedCount;
    private long underTreatmentCount;
    private long completedCount;
    private long criticalCount;
}
