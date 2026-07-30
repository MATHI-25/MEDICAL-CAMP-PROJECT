package com.mediq.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GlobalSearchResultResponse {

    private String query;
    private List<PatientResponse> patients;
    private List<UserResponse> users;
    private List<CampResponse> camps;
    private List<PrescriptionResponse> prescriptions;
    private List<ReferralResponse> referrals;
    private List<MedicineResponse> medicines;
}
