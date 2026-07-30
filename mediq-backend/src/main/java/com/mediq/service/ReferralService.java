package com.mediq.service;

import com.mediq.constants.ReferralStatus;
import com.mediq.dto.CreateReferralRequest;
import com.mediq.dto.ReferralResponse;
import com.mediq.dto.UpdateReferralStatusRequest;

import java.util.List;

public interface ReferralService {

    ReferralResponse createReferral(CreateReferralRequest request);

    ReferralResponse updateReferralStatus(Long referralDbId, UpdateReferralStatusRequest request);

    ReferralResponse getReferralById(Long referralDbId);

    ReferralResponse getReferralByCode(String referralId);

    ReferralResponse getReferralByConsultationId(Long consultationId);

    List<ReferralResponse> getPatientReferrals(Long patientId);

    List<ReferralResponse> getReferralsByStatus(Long campId, ReferralStatus status);

    byte[] generateReferralPdf(Long referralDbId);
}
