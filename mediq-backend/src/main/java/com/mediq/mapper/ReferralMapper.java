package com.mediq.mapper;

import com.mediq.dto.ReferralResponse;
import com.mediq.entity.HospitalReferral;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReferralMapper {

    private final PatientMapper patientMapper;
    private final UserMapper userMapper;

    public ReferralResponse toResponse(HospitalReferral referral) {
        if (referral == null) {
            return null;
        }

        return ReferralResponse.builder()
                .id(referral.getId())
                .referralId(referral.getReferralId())
                .patient(patientMapper.toResponse(referral.getPatient()))
                .doctor(userMapper.toResponse(referral.getDoctor()))
                .campId(referral.getCamp() != null ? referral.getCamp().getId() : null)
                .campTitle(referral.getCamp() != null ? referral.getCamp().getTitle() : null)
                .consultationId(referral.getConsultation() != null ? referral.getConsultation().getId() : null)
                .consultationCode(referral.getConsultation() != null ? referral.getConsultation().getConsultationCode() : null)
                .reason(referral.getReason())
                .recommendedTests(referral.getRecommendedTests())
                .hospitalName(referral.getHospitalName())
                .hospitalAddress(referral.getHospitalAddress())
                .department(referral.getDepartment())
                .specialistType(referral.getSpecialistType())
                .doctorNotes(referral.getDoctorNotes())
                .currentMedicines(referral.getCurrentMedicines())
                .urgency(referral.getUrgency())
                .followUpDate(referral.getFollowUpDate())
                .remarks(referral.getRemarks())
                .status(referral.getStatus())
                .createdAt(referral.getCreatedAt())
                .build();
    }
}
