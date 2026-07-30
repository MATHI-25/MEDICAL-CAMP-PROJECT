package com.mediq.repository;

import com.mediq.constants.PrescriptionStatus;
import com.mediq.entity.Prescription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {

    Optional<Prescription> findByPrescriptionCodeAndIsDeletedFalse(String prescriptionCode);

    Optional<Prescription> findByConsultationIdAndIsDeletedFalse(Long consultationId);

    List<Prescription> findByPatientIdAndIsDeletedFalseOrderByCreatedAtDesc(Long patientId);

    List<Prescription> findByCampIdAndStatusAndIsDeletedFalseOrderByCreatedAtDesc(Long campId, PrescriptionStatus status);

    long countByCampIdAndIsDeletedFalse(Long campId);

    long countByDoctorIdAndCampIdAndIsDeletedFalse(Long doctorId, Long campId);
}
