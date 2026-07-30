package com.mediq.repository;

import com.mediq.entity.Consultation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConsultationRepository extends JpaRepository<Consultation, Long> {

    Optional<Consultation> findByConsultationCodeAndIsDeletedFalse(String consultationCode);

    Optional<Consultation> findByQueueTokenIdAndIsDeletedFalse(Long queueTokenId);

    List<Consultation> findByPatientIdAndIsDeletedFalseOrderByCreatedAtDesc(Long patientId);

    List<Consultation> findByDoctorIdAndCampIdAndIsDeletedFalseOrderByCreatedAtDesc(Long doctorId, Long campId);

    long countByCampIdAndIsDeletedFalse(Long campId);

    long countByDoctorIdAndCampIdAndIsDeletedFalse(Long doctorId, Long campId);
}
