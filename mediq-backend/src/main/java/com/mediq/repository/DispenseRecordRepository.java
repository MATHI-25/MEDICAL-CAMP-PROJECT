package com.mediq.repository;

import com.mediq.entity.DispenseRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DispenseRecordRepository extends JpaRepository<DispenseRecord, Long> {

    List<DispenseRecord> findByPrescriptionIdAndIsDeletedFalse(Long prescriptionId);

    List<DispenseRecord> findByPharmacistIdAndIsDeletedFalseOrderByDispenseDateDesc(Long pharmacistId);
}
