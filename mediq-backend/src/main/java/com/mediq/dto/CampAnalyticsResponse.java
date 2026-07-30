package com.mediq.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampAnalyticsResponse {

    private Long campId;
    private String campTitle;
    private String campCode;
    private String location;
    private long totalPatientsRegistered;
    private long totalVitalsRecorded;
    private long totalConsultationsCompleted;
    private long totalPrescriptionsIssued;
    private long totalReferralsIssued;
    private long totalMedicinesDispensed;
    private double completionRatePercentage;
}
