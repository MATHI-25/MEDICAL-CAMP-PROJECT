package com.mediq.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Entity
@Table(name = "consultations")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Consultation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "consultation_code", nullable = false, unique = true, length = 50)
    private String consultationCode;

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
    @JoinColumn(name = "queue_token_id", nullable = false)
    private QueueToken queueToken;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vitals_id")
    private PatientVitals vitals;

    @Column(name = "disease_name", length = 150)
    private String diseaseName;

    @Column(name = "diagnosis_notes", nullable = false, columnDefinition = "TEXT")
    private String diagnosisNotes;

    @Column(name = "lab_test_recommendations", columnDefinition = "TEXT")
    private String labTestRecommendations;

    @Column(name = "doctor_notes", columnDefinition = "TEXT")
    private String doctorNotes;

    @Column(name = "follow_up_date")
    private LocalDate followUpDate;

    @Builder.Default
    @Column(name = "requires_referral")
    private Boolean requiresReferral = false;
}
