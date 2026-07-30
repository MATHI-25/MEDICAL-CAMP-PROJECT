package com.mediq.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Entity
@Table(name = "medical_histories")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class MedicalHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType;

    @Column(name = "event_summary", nullable = false, length = 255)
    private String eventSummary;

    @Column(name = "event_details", columnDefinition = "TEXT")
    private String eventDetails;

    @Column(name = "reference_code", length = 50)
    private String referenceCode;

    @Column(name = "performed_by", length = 100)
    private String performedBy;

    @Builder.Default
    @Column(name = "event_timestamp", nullable = false)
    private LocalDateTime eventTimestamp = LocalDateTime.now();
}
