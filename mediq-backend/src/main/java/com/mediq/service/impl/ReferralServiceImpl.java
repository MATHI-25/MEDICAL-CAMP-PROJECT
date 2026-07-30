package com.mediq.service.impl;

import com.mediq.constants.QueueStatus;
import com.mediq.constants.ReferralStatus;
import com.mediq.dto.CreateReferralRequest;
import com.mediq.dto.ReferralResponse;
import com.mediq.dto.UpdateReferralStatusRequest;
import com.mediq.entity.*;
import com.mediq.exception.ResourceNotFoundException;
import com.mediq.mapper.ReferralMapper;
import com.mediq.repository.*;
import com.mediq.service.MedicalHistoryService;
import com.mediq.service.ReferralService;
import com.mediq.util.PdfGeneratorUtil;
import com.mediq.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReferralServiceImpl implements ReferralService {

    private final HospitalReferralRepository referralRepository;
    private final ConsultationRepository consultationRepository;
    private final PatientRepository patientRepository;
    private final CampRepository campRepository;
    private final UserRepository userRepository;
    private final QueueTokenRepository queueTokenRepository;
    private final ReferralMapper referralMapper;
    private final PdfGeneratorUtil pdfGeneratorUtil;
    private final MedicalHistoryService medicalHistoryService;

    @Override
    @Transactional
    public ReferralResponse createReferral(CreateReferralRequest request) {
        Patient patient = patientRepository.findById(request.getPatientId())
                .filter(p -> !p.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Patient", "id", request.getPatientId()));

        MedicalCamp camp = campRepository.findById(request.getCampId())
                .filter(c -> !c.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("MedicalCamp", "id", request.getCampId()));

        Consultation consultation = consultationRepository.findById(request.getConsultationId())
                .filter(c -> !c.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Consultation", "id", request.getConsultationId()));

        String currentMemberId = SecurityUtils.getCurrentUserMemberId()
                .orElseThrow(() -> new ResourceNotFoundException("Logged in doctor session missing"));

        User doctor = userRepository.findByMemberIdAndIsDeletedFalse(currentMemberId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "memberId", currentMemberId));

        HospitalReferral existing = referralRepository.findByConsultationIdAndIsDeletedFalse(consultation.getId()).orElse(null);
        HospitalReferral savedReferral;
        String finalReferralId;

        if (existing != null) {
            existing.setHospitalName(request.getHospitalName());
            existing.setHospitalAddress(request.getHospitalAddress());
            existing.setDepartment(request.getDepartment());
            existing.setSpecialistType(request.getSpecialistType());
            existing.setReason(request.getReason());
            existing.setRecommendedTests(request.getRecommendedTests());
            existing.setDoctorNotes(request.getDoctorNotes());
            existing.setCurrentMedicines(request.getCurrentMedicines());
            existing.setUrgency(request.getUrgency() != null ? request.getUrgency() : "NORMAL");
            existing.setFollowUpDate(request.getFollowUpDate());
            existing.setRemarks(request.getRemarks());
            savedReferral = referralRepository.save(existing);
            finalReferralId = existing.getReferralId();
        } else {
            String generatedReferralId = generateReferralId(camp.getId());
            HospitalReferral referral = HospitalReferral.builder()
                    .referralId(generatedReferralId)
                    .patient(patient)
                    .doctor(doctor)
                    .camp(camp)
                    .consultation(consultation)
                    .reason(request.getReason())
                    .recommendedTests(request.getRecommendedTests())
                    .hospitalName(request.getHospitalName())
                    .hospitalAddress(request.getHospitalAddress())
                    .department(request.getDepartment())
                    .specialistType(request.getSpecialistType())
                    .doctorNotes(request.getDoctorNotes())
                    .currentMedicines(request.getCurrentMedicines())
                    .urgency(request.getUrgency() != null ? request.getUrgency() : "NORMAL")
                    .followUpDate(request.getFollowUpDate())
                    .remarks(request.getRemarks())
                    .status(ReferralStatus.CREATED)
                    .build();
            savedReferral = referralRepository.save(referral);
            finalReferralId = generatedReferralId;
        }

        // Update Queue Token to REFERRED_TO_HOSPITAL or COMPLETED
        QueueToken token = consultation.getQueueToken();
        if (token != null) {
            token.setStatus(QueueStatus.REFERRED_TO_HOSPITAL);
            queueTokenRepository.save(token);
        }

        // Record referral event in patient medical history timeline
        medicalHistoryService.recordEvent(
                patient,
                "REFERRAL",
                String.format("Hospital Referral Issued: %s to %s (%s)", finalReferralId, request.getHospitalName(), request.getDepartment()),
                String.format("Referred by Dr. %s. Reason: %s. Urgency: %s.", doctor.getFullName(), request.getReason(), savedReferral.getUrgency()),
                finalReferralId,
                doctor.getFullName()
        );

        return referralMapper.toResponse(savedReferral);
    }

    @Override
    @Transactional
    public ReferralResponse updateReferralStatus(Long referralDbId, UpdateReferralStatusRequest request) {
        HospitalReferral referral = findReferralById(referralDbId);
        referral.setStatus(request.getStatus());
        if (request.getRemarks() != null && !request.getRemarks().isBlank()) {
            referral.setRemarks(request.getRemarks());
        }
        return referralMapper.toResponse(referralRepository.save(referral));
    }

    @Override
    @Transactional(readOnly = true)
    public ReferralResponse getReferralById(Long referralDbId) {
        return referralMapper.toResponse(findReferralById(referralDbId));
    }

    @Override
    @Transactional(readOnly = true)
    public ReferralResponse getReferralByCode(String referralId) {
        HospitalReferral referral = referralRepository.findByReferralIdAndIsDeletedFalse(referralId)
                .orElseThrow(() -> new ResourceNotFoundException("HospitalReferral", "referralId", referralId));
        return referralMapper.toResponse(referral);
    }

    @Override
    @Transactional(readOnly = true)
    public ReferralResponse getReferralByConsultationId(Long consultationId) {
        HospitalReferral referral = referralRepository.findByConsultationIdAndIsDeletedFalse(consultationId)
                .orElseThrow(() -> new ResourceNotFoundException("HospitalReferral", "consultationId", consultationId));
        return referralMapper.toResponse(referral);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReferralResponse> getPatientReferrals(Long patientId) {
        return referralRepository.findByPatientIdAndIsDeletedFalseOrderByCreatedAtDesc(patientId)
                .stream()
                .map(referralMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReferralResponse> getReferralsByStatus(Long campId, ReferralStatus status) {
        return referralRepository.findByCampIdAndStatusAndIsDeletedFalseOrderByCreatedAtDesc(campId, status)
                .stream()
                .map(referralMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] generateReferralPdf(Long referralDbId) {
        HospitalReferral referral = findReferralById(referralDbId);
        return pdfGeneratorUtil.generateReferralLetterPdf(referral);
    }

    private HospitalReferral findReferralById(Long referralDbId) {
        return referralRepository.findById(referralDbId)
                .filter(r -> !r.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("HospitalReferral", "id", referralDbId));
    }

    private synchronized String generateReferralId(Long campId) {
        int year = LocalDate.now().getYear();
        long count = referralRepository.countByCampIdAndIsDeletedFalse(campId) + 1;
        String candidate = String.format("REF-%d-%04d", year, count);
        while (referralRepository.findByReferralIdAndIsDeletedFalse(candidate).isPresent()) {
            count++;
            candidate = String.format("REF-%d-%04d", year, count);
        }
        return candidate;
    }
}
