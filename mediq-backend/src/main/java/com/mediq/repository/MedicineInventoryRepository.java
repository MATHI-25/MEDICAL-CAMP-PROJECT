package com.mediq.repository;

import com.mediq.entity.MedicineInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MedicineInventoryRepository extends JpaRepository<MedicineInventory, Long> {

    Optional<MedicineInventory> findByMedicineCodeAndIsDeletedFalse(String medicineCode);

    boolean existsByMedicineCodeAndIsDeletedFalse(String medicineCode);

    List<MedicineInventory> findByNameContainingIgnoreCaseAndIsDeletedFalse(String name);

    @Query("SELECT m FROM MedicineInventory m WHERE m.isDeleted = false AND m.stockQuantity <= m.minAlertQuantity ORDER BY m.stockQuantity ASC")
    List<MedicineInventory> findLowStockMedicines();

    List<MedicineInventory> findByExpiryDateBeforeAndIsDeletedFalseOrderByExpiryDateAsc(LocalDate date);
}
