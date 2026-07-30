package com.mediq.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Entity
@Table(name = "medicine_inventory")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class MedicineInventory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "medicine_code", nullable = false, unique = true, length = 50)
    private String medicineCode;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "category", length = 100)
    private String category;

    @Column(name = "batch_number", nullable = false, length = 50)
    private String batchNumber;

    @Column(name = "manufacturer", length = 100)
    private String manufacturer;

    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    @Builder.Default
    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity = 0;

    @Builder.Default
    @Column(name = "min_alert_quantity", nullable = false)
    private Integer minAlertQuantity = 20;

    @Builder.Default
    @Column(name = "unit_price")
    private Double unitPrice = 0.0;
}
