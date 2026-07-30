package com.mediq.entity;

import com.mediq.constants.ReferralStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Entity
@Table(name = "hospital_referrals")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class HospitalReferral extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "referral_id", nullable = false, unique = true, length = 50)
    private String referralId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private User doctor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "camp_id", nullable = false)
    private MedicalCamp camp;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consultation_id", nullable = false)
    private Consultation consultation;

    @Column(name = "reason", nullable = false, columnDefinition = "TEXT")
    private String reason;

    @Column(name = "recommended_tests", columnDefinition = "TEXT")
    private String recommendedTests;

    @Column(name = "hospital_name", nullable = false, length = 150)
    private String hospitalName;

    @Column(name = "hospital_address", columnDefinition = "TEXT")
    private String hospitalAddress;

    @Column(name = "department", nullable = false, length = 100)
    private String department;

    @Column(name = "specialist_type", length = 100)
    private String specialistType;

    @Column(name = "doctor_notes", columnDefinition = "TEXT")
    private String doctorNotes;

    @Column(name = "current_medicines", columnDefinition = "TEXT")
    private String currentMedicines;

    @Builder.Default
    @Column(name = "urgency", nullable = false, length = 30)
    private String urgency = "NORMAL";

    @Column(name = "follow_up_date")
    private LocalDate followUpDate;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private ReferralStatus status = ReferralStatus.CREATED;
}
