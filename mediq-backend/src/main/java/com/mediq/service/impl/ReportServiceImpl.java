package com.mediq.service.impl;

import com.mediq.constants.QueueStatus;
import com.mediq.constants.ReferralStatus;
import com.mediq.constants.UserRole;
import com.mediq.dto.*;
import com.mediq.entity.MedicalCamp;
import com.mediq.entity.User;
import com.mediq.exception.ResourceNotFoundException;
import com.mediq.mapper.*;
import com.mediq.repository.*;
import com.mediq.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final CampRepository campRepository;
    private final PatientRepository patientRepository;
    private final PatientVitalsRepository vitalsRepository;
    private final ConsultationRepository consultationRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final HospitalReferralRepository referralRepository;
    private final QueueTokenRepository queueTokenRepository;
    private final MedicineInventoryRepository medicineRepository;
    private final UserRepository userRepository;

    private final PatientMapper patientMapper;
    private final UserMapper userMapper;
    private final CampMapper campMapper;
    private final PrescriptionMapper prescriptionMapper;
    private final ReferralMapper referralMapper;
    private final MedicineMapper medicineMapper;

    @Override
    @Transactional(readOnly = true)
    public CampAnalyticsResponse getCampAnalytics(Long campId) {
        MedicalCamp camp = campRepository.findById(campId)
                .filter(c -> !c.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("MedicalCamp", "id", campId));

        long patients = patientRepository.countByRegisteredCampIdAndIsDeletedFalse(campId);
        long vitals = vitalsRepository.findByCampIdAndIsDeletedFalseOrderByCreatedAtDesc(campId).size();
        long consultations = consultationRepository.countByCampIdAndIsDeletedFalse(campId);
        long prescriptions = prescriptionRepository.countByCampIdAndIsDeletedFalse(campId);
        long referrals = referralRepository.countByCampIdAndIsDeletedFalse(campId);

        long totalTokens = queueTokenRepository.countByCampIdAndIsDeletedFalse(campId);
        long completedTokens = queueTokenRepository.countByCampIdAndStatusAndIsDeletedFalse(campId, QueueStatus.COMPLETED)
                + queueTokenRepository.countByCampIdAndStatusAndIsDeletedFalse(campId, QueueStatus.SENT_TO_PHARMACY)
                + queueTokenRepository.countByCampIdAndStatusAndIsDeletedFalse(campId, QueueStatus.REFERRED_TO_HOSPITAL);

        long totalBase = totalTokens > 0 ? totalTokens : patients;
        long completedBase = totalTokens > 0 ? completedTokens : consultations;

        double completionRate = totalBase > 0 ? ((double) completedBase / totalBase) * 100.0 : 0.0;
        if (completionRate > 100.0) completionRate = 100.0;

        return CampAnalyticsResponse.builder()
                .campId(camp.getId())
                .campTitle(camp.getTitle())
                .campCode(camp.getCampCode())
                .location(camp.getLocation())
                .totalPatientsRegistered(patients)
                .totalVitalsRecorded(vitals)
                .totalConsultationsCompleted(consultations)
                .totalPrescriptionsIssued(prescriptions)
                .totalReferralsIssued(referrals)
                .totalMedicinesDispensed(prescriptions)
                .completionRatePercentage(Math.round(completionRate * 10.0) / 10.0)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DoctorReportResponse> getDoctorWorkloadReport(Long campId) {
        MedicalCamp camp = campRepository.findById(campId)
                .filter(c -> !c.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("MedicalCamp", "id", campId));

        java.util.Set<User> doctorSet = new java.util.LinkedHashSet<>(camp.getAssignedDoctors());
        List<User> allSystemDoctors = userRepository.findByRoleAndIsActiveTrueAndIsDeletedFalse(UserRole.DOCTOR);
        doctorSet.addAll(allSystemDoctors);

        List<DoctorReportResponse> list = new ArrayList<>();
        for (User doctor : doctorSet) {
            if (doctor != null && !doctor.isDeleted()) {
                long consultations = consultationRepository.countByDoctorIdAndCampIdAndIsDeletedFalse(doctor.getId(), campId);
                long prescriptions = prescriptionRepository.countByDoctorIdAndCampIdAndIsDeletedFalse(doctor.getId(), campId);
                long referrals = referralRepository.countByDoctorIdAndCampIdAndIsDeletedFalse(doctor.getId(), campId);

                boolean isAssigned = camp.getAssignedDoctors().stream().anyMatch(d -> d.getId().equals(doctor.getId()));
                if (consultations > 0 || prescriptions > 0 || referrals > 0 || isAssigned) {
                    list.add(DoctorReportResponse.builder()
                            .doctorId(doctor.getId())
                            .doctorMemberId(doctor.getMemberId())
                            .doctorName(doctor.getFullName())
                            .specialization(doctor.getSpecialization() != null ? doctor.getSpecialization() : "General Physician")
                            .totalConsultations(consultations)
                            .totalPrescriptions(prescriptions)
                            .totalReferrals(referrals)
                            .build());
                }
            }
        }
        return list;
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicineReportResponse> getMedicineConsumptionReport() {
        return medicineRepository.findAll()
                .stream()
                .filter(m -> !m.isDeleted())
                .map(m -> MedicineReportResponse.builder()
                        .medicineId(m.getId())
                        .medicineCode(m.getMedicineCode())
                        .name(m.getName())
                        .category(m.getCategory())
                        .currentStock(m.getStockQuantity())
                        .minAlertQuantity(m.getMinAlertQuantity())
                        .isLowStock(m.getStockQuantity() <= m.getMinAlertQuantity())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ReferralReportResponse getReferralReport(Long campId) {
        long total = referralRepository.countByCampIdAndIsDeletedFalse(campId);
        long created = referralRepository.findByCampIdAndStatusAndIsDeletedFalseOrderByCreatedAtDesc(campId, ReferralStatus.CREATED).size();
        long sent = referralRepository.findByCampIdAndStatusAndIsDeletedFalseOrderByCreatedAtDesc(campId, ReferralStatus.SENT).size();
        long visited = referralRepository.findByCampIdAndStatusAndIsDeletedFalseOrderByCreatedAtDesc(campId, ReferralStatus.VISITED).size();
        long treatment = referralRepository.findByCampIdAndStatusAndIsDeletedFalseOrderByCreatedAtDesc(campId, ReferralStatus.UNDER_TREATMENT).size();
        long completed = referralRepository.findByCampIdAndStatusAndIsDeletedFalseOrderByCreatedAtDesc(campId, ReferralStatus.COMPLETED).size();

        long critical = referralRepository.findByCampIdAndStatusAndIsDeletedFalseOrderByCreatedAtDesc(campId, null)
                .stream().filter(r -> "CRITICAL".equalsIgnoreCase(r.getUrgency())).count();

        return ReferralReportResponse.builder()
                .campId(campId)
                .totalReferrals(total)
                .createdCount(created)
                .sentCount(sent)
                .visitedCount(visited)
                .underTreatmentCount(treatment)
                .completedCount(completed)
                .criticalCount(critical)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public GlobalSearchResultResponse globalSearch(String query) {
        if (query == null || query.isBlank()) {
            return GlobalSearchResultResponse.builder()
                    .query("")
                    .patients(List.of())
                    .users(List.of())
                    .camps(List.of())
                    .prescriptions(List.of())
                    .referrals(List.of())
                    .medicines(List.of())
                    .build();
        }

        String keyword = query.trim();

        List<PatientResponse> patients = patientRepository.searchPatients(null, keyword, org.springframework.data.domain.PageRequest.of(0, 5))
                .stream().map(patientMapper::toResponse).collect(Collectors.toList());

        List<UserResponse> users = userRepository.searchUsers(null, keyword, org.springframework.data.domain.PageRequest.of(0, 5))
                .stream().map(userMapper::toResponse).collect(Collectors.toList());

        List<CampResponse> camps = campRepository.searchCamps(null, keyword, org.springframework.data.domain.PageRequest.of(0, 5))
                .stream().map(campMapper::toResponse).collect(Collectors.toList());

        List<PrescriptionResponse> prescriptions = prescriptionRepository.findByPrescriptionCodeAndIsDeletedFalse(keyword)
                .map(p -> List.of(prescriptionMapper.toResponse(p)))
                .orElse(List.of());

        List<ReferralResponse> referrals = referralRepository.findByReferralIdAndIsDeletedFalse(keyword)
                .map(r -> List.of(referralMapper.toResponse(r)))
                .orElse(List.of());

        List<MedicineResponse> medicines = medicineRepository.findByNameContainingIgnoreCaseAndIsDeletedFalse(keyword)
                .stream().map(medicineMapper::toResponse).collect(Collectors.toList());

        return GlobalSearchResultResponse.builder()
                .query(keyword)
                .patients(patients)
                .users(users)
                .camps(camps)
                .prescriptions(prescriptions)
                .referrals(referrals)
                .medicines(medicines)
                .build();
    }
}
