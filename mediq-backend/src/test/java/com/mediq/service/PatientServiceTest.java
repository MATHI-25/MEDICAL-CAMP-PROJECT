package com.mediq.service;

import com.mediq.dto.PatientResponse;
import com.mediq.dto.RegisterPatientRequest;
import com.mediq.entity.MedicalCamp;
import com.mediq.entity.Patient;
import com.mediq.mapper.PatientMapper;
import com.mediq.repository.CampRepository;
import com.mediq.repository.PatientRepository;
import com.mediq.service.impl.PatientServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatientServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private CampRepository campRepository;

    @Mock
    private PatientMapper patientMapper;

    @Mock
    private MedicalHistoryService medicalHistoryService;

    @InjectMocks
    private PatientServiceImpl patientService;

    private MedicalCamp sampleCamp;
    private Patient samplePatient;

    @BeforeEach
    void setUp() {
        sampleCamp = MedicalCamp.builder()
                .id(1L)
                .campCode("CAMP-2026-001")
                .title("Sector 12 Free Health Camp")
                .build();

        samplePatient = Patient.builder()
                .id(10L)
                .patientId("PAT-2026-0001")
                .fullName("Robert Miller")
                .age(45)
                .gender("MALE")
                .registeredCamp(sampleCamp)
                .build();
    }

    @Test
    void testRegisterPatientSuccess() {
        RegisterPatientRequest request = RegisterPatientRequest.builder()
                .fullName("Robert Miller")
                .age(45)
                .gender("MALE")
                .phone("+1-555-0199")
                .registeredCampId(1L)
                .build();

        PatientResponse expectedResponse = PatientResponse.builder()
                .id(10L)
                .patientId("PAT-2026-0001")
                .fullName("Robert Miller")
                .build();

        when(campRepository.findById(1L)).thenReturn(Optional.of(sampleCamp));
        when(patientRepository.countByRegisteredCampIdAndIsDeletedFalse(1L)).thenReturn(0L);
        when(patientRepository.save(any(Patient.class))).thenReturn(samplePatient);
        when(patientMapper.toResponse(samplePatient)).thenReturn(expectedResponse);

        PatientResponse response = patientService.registerPatient(request);

        assertNotNull(response);
        assertEquals("PAT-2026-0001", response.getPatientId());
        assertEquals("Robert Miller", response.getFullName());
        verify(patientRepository, times(1)).save(any());
        verify(medicalHistoryService, times(1)).recordEvent(any(), eq("REGISTRATION"), any(), any(), any(), any());
    }
}
