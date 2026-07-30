package com.mediq.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterPatientRequest {

    @NotBlank(message = "Patient full name is required")
    private String fullName;

    @NotNull(message = "Age is required")
    @Min(value = 0, message = "Age cannot be negative")
    @Max(value = 150, message = "Invalid age")
    private Integer age;

    @NotBlank(message = "Gender is required")
    private String gender;

    private String bloodGroup;

    private String phone;

    private String address;

    private String emergencyContact;

    private String allergies;

    private String chronicDiseases;

    @NotNull(message = "Camp ID is required")
    private Long registeredCampId;
}
