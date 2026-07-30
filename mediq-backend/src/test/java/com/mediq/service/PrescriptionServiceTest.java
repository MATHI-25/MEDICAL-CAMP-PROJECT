package com.mediq.service;

import com.mediq.constants.PrescriptionStatus;
import com.mediq.entity.MedicalCamp;
import com.mediq.entity.Patient;
import com.mediq.entity.Prescription;
import com.mediq.entity.User;
import com.mediq.repository.PrescriptionRepository;
import com.mediq.service.impl.PrescriptionServiceImpl;
import com.mediq.util.PdfGeneratorUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PrescriptionServiceTest {

    @Mock
    private PrescriptionRepository prescriptionRepository;

    @Mock
    private PdfGeneratorUtil pdfGeneratorUtil;

    @InjectMocks
    private PrescriptionServiceImpl prescriptionService;

    private Prescription samplePrescription;

    @BeforeEach
    void setUp() {
        Patient patient = Patient.builder()
                .patientId("PAT-2026-0001")
                .fullName("Robert Miller")
                .age(45)
                .gender("MALE")
                .build();

        User doctor = User.builder()
                .memberId("MC-DOC-001")
                .fullName("Dr. Sarah Jenkins")
                .build();

        MedicalCamp camp = MedicalCamp.builder()
                .title("Sector 12 Community Health Camp")
                .location("Sector 12")
                .build();

        samplePrescription = Prescription.builder()
                .id(100L)
                .prescriptionCode("RX-2026-0001")
                .patient(patient)
                .doctor(doctor)
                .camp(camp)
                .status(PrescriptionStatus.CREATED)
                .build();
    }

    @Test
    void testGeneratePrescriptionPdf() {
        byte[] expectedPdfBytes = "Mock PDF Byte Content".getBytes();

        when(prescriptionRepository.findById(100L)).thenReturn(Optional.of(samplePrescription));
        when(pdfGeneratorUtil.generatePrescriptionPdf(samplePrescription)).thenReturn(expectedPdfBytes);

        byte[] result = prescriptionService.generatePrescriptionPdf(100L);

        assertNotNull(result);
        assertTrue(result.length > 0);
        verify(pdfGeneratorUtil, times(1)).generatePrescriptionPdf(samplePrescription);
    }
}
