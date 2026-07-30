package com.mediq.repository;

import com.mediq.entity.PatientVitals;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PatientVitalsRepository extends JpaRepository<PatientVitals, Long> {

    Optional<PatientVitals> findByQueueTokenIdAndIsDeletedFalse(Long queueTokenId);

    List<PatientVitals> findByPatientIdAndIsDeletedFalseOrderByCreatedAtDesc(Long patientId);

    List<PatientVitals> findByCampIdAndIsDeletedFalseOrderByCreatedAtDesc(Long campId);
}
