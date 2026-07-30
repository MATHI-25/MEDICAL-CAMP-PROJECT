package com.mediq.repository;

import com.mediq.constants.QueueStatus;
import com.mediq.entity.QueueToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QueueTokenRepository extends JpaRepository<QueueToken, Long> {

    List<QueueToken> findByCampIdAndStatusAndIsDeletedFalseOrderBySequenceNumberAsc(Long campId, QueueStatus status);

    List<QueueToken> findByCampIdAndAssignedDoctorIdAndStatusAndIsDeletedFalseOrderBySequenceNumberAsc(Long campId, Long doctorId, QueueStatus status);

    @Query("SELECT q FROM QueueToken q WHERE q.camp.id = :campId AND (q.assignedDoctor.id = :doctorId OR q.assignedDoctor IS NULL) AND q.status IN ('WAITING_FOR_DOCTOR', 'IN_CONSULTATION') AND q.isDeleted = false ORDER BY q.sequenceNumber ASC")
    List<QueueToken> findDoctorQueueTokens(@Param("campId") Long campId, @Param("doctorId") Long doctorId);

    @Query("SELECT q FROM QueueToken q WHERE q.camp.id = :campId AND q.status IN ('WAITING', 'IN_VITALS') AND q.isDeleted = false ORDER BY q.sequenceNumber ASC")
    List<QueueToken> findNurseQueueTokens(@Param("campId") Long campId);

    Optional<QueueToken> findTopByCampIdAndIsDeletedFalseOrderBySequenceNumberDesc(Long campId);

    List<QueueToken> findByPatientIdAndIsDeletedFalseOrderByCreatedAtDesc(Long patientId);

    long countByCampIdAndStatusAndIsDeletedFalse(Long campId, QueueStatus status);

    long countByCampIdAndIsDeletedFalse(Long campId);
}
