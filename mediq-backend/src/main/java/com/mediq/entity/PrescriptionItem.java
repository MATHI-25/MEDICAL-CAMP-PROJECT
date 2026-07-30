package com.mediq.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "prescription_items")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class PrescriptionItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prescription_id", nullable = false)
    private Prescription prescription;

    @Column(name = "medicine_name", nullable = false, length = 150)
    private String medicineName;

    @Column(name = "dosage", nullable = false, length = 50)
    private String dosage;

    @Column(name = "frequency", nullable = false, length = 50)
    private String frequency;

    @Column(name = "duration", nullable = false, length = 50)
    private String duration;

    @Column(name = "instructions", length = 255)
    private String instructions;

    @Column(name = "quantity_prescribed", nullable = false)
    private Integer quantityPrescribed;

    @Builder.Default
    @Column(name = "quantity_dispensed", nullable = false)
    private Integer quantityDispensed = 0;
}
