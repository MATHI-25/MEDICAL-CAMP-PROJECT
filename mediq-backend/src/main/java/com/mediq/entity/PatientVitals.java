package com.mediq.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "patient_vitals")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class PatientVitals extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "queue_token_id", nullable = false)
    private QueueToken queueToken;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "camp_id", nullable = false)
    private MedicalCamp camp;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recorded_by_nurse_id", nullable = false)
    private User recordedByNurse;

    @Column(name = "height_cm")
    private Double heightCm;

    @Column(name = "weight_kg")
    private Double weightKg;

    @Column(name = "bmi")
    private Double bmi;

    @Column(name = "temperature_f")
    private Double temperatureF;

    @Column(name = "blood_pressure", length = 20)
    private String bloodPressure;

    @Column(name = "pulse_rate")
    private Integer pulseRate;

    @Column(name = "respiratory_rate")
    private Integer respiratoryRate;

    @Column(name = "blood_sugar_mg_dl")
    private Double bloodSugarMgDl;

    @Column(name = "spo2_percent")
    private Integer spo2Percent;

    @Column(name = "symptoms", columnDefinition = "TEXT")
    private String symptoms;

    @Builder.Default
    @Column(name = "pain_scale")
    private Integer painScale = 0;

    @Column(name = "nurse_notes", columnDefinition = "TEXT")
    private String nurseNotes;
}
