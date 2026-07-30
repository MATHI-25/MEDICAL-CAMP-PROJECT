package com.mediq.repository;

import com.mediq.constants.CampStatus;
import com.mediq.entity.MedicalCamp;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CampRepository extends JpaRepository<MedicalCamp, Long> {

    Optional<MedicalCamp> findByCampCodeAndIsDeletedFalse(String campCode);

    boolean existsByCampCodeAndIsDeletedFalse(String campCode);

    List<MedicalCamp> findByStatusAndIsDeletedFalseOrderByStartDateAsc(CampStatus status);

    @Query("SELECT c FROM MedicalCamp c WHERE c.isDeleted = false AND " +
           "(:status IS NULL OR c.status = :status) AND " +
           "(:keyword IS NULL OR LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(c.location) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(c.campCode) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<MedicalCamp> searchCamps(@Param("status") CampStatus status, @Param("keyword") String keyword, Pageable pageable);
}
