package com.mediq.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorReportResponse {

    private Long doctorId;
    private String doctorMemberId;
    private String doctorName;
    private String specialization;
    private long totalConsultations;
    private long totalPrescriptions;
    private long totalReferrals;
}
