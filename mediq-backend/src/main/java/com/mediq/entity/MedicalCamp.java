package com.mediq.entity;

import com.mediq.constants.CampStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "camps")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class MedicalCamp extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "camp_code", nullable = false, unique = true, length = 50)
    private String campCode;

    @Column(name = "title", nullable = false, length = 150)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "location", nullable = false)
    private String location;

    @Column(name = "venue", length = 150)
    private String venue;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "start_time", length = 30)
    private String startTime;

    @Column(name = "end_time", length = 30)
    private String endTime;

    @Column(name = "operating_hours", length = 100)
    private String operatingHours;

    @Builder.Default
    @Column(name = "target_capacity")
    private Integer targetCapacity = 500;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private CampStatus status = CampStatus.UPCOMING;

    @Builder.Default
    @ManyToMany
    @JoinTable(
        name = "camp_doctors",
        joinColumns = @JoinColumn(name = "camp_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> assignedDoctors = new HashSet<>();

    @Builder.Default
    @ManyToMany
    @JoinTable(
        name = "camp_nurses",
        joinColumns = @JoinColumn(name = "camp_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> assignedNurses = new HashSet<>();

    @Builder.Default
    @ManyToMany
    @JoinTable(
        name = "camp_volunteers",
        joinColumns = @JoinColumn(name = "camp_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> assignedVolunteers = new HashSet<>();
}
