package com.mediq.repository;

import com.mediq.constants.ReferralStatus;
import com.mediq.entity.HospitalReferral;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HospitalReferralRepository extends JpaRepository<HospitalReferral, Long> {

    Optional<HospitalReferral> findByReferralIdAndIsDeletedFalse(String referralId);

    Optional<HospitalReferral> findByConsultationIdAndIsDeletedFalse(Long consultationId);

    List<HospitalReferral> findByPatientIdAndIsDeletedFalseOrderByCreatedAtDesc(Long patientId);

    List<HospitalReferral> findByCampIdAndStatusAndIsDeletedFalseOrderByCreatedAtDesc(Long campId, ReferralStatus status);

    List<HospitalReferral> findByDoctorIdAndCampIdAndIsDeletedFalseOrderByCreatedAtDesc(Long doctorId, Long campId);

    long countByCampIdAndIsDeletedFalse(Long campId);

    long countByDoctorIdAndCampIdAndIsDeletedFalse(Long doctorId, Long campId);
}
