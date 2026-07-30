package com.mediq.repository;

import com.mediq.entity.Patient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

    Optional<Patient> findByPatientIdAndIsDeletedFalse(String patientId);

    boolean existsByPatientIdAndIsDeletedFalse(String patientId);

    List<Patient> findByPhoneAndIsDeletedFalse(String phone);

    List<Patient> findByRegisteredCampIdAndIsDeletedFalse(Long campId);

    long countByRegisteredCampIdAndIsDeletedFalse(Long campId);

    @Query("SELECT p FROM Patient p WHERE p.isDeleted = false AND " +
           "(:campId IS NULL OR p.registeredCamp.id = :campId) AND " +
           "(:keyword IS NULL OR LOWER(p.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.patientId) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.phone) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Patient> searchPatients(@Param("campId") Long campId, @Param("keyword") String keyword, Pageable pageable);
}
