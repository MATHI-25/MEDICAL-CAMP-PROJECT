package com.mediq.service;

import com.mediq.constants.PrescriptionStatus;
import com.mediq.dto.CreatePrescriptionRequest;
import com.mediq.dto.PrescriptionResponse;

import java.util.List;

public interface PrescriptionService {

    PrescriptionResponse createPrescription(CreatePrescriptionRequest request);

    PrescriptionResponse getPrescriptionById(Long prescriptionId);

    PrescriptionResponse getPrescriptionByCode(String prescriptionCode);

    PrescriptionResponse getPrescriptionByConsultationId(Long consultationId);

    List<PrescriptionResponse> getPatientPrescriptions(Long patientId);

    List<PrescriptionResponse> getPrescriptionsByStatus(Long campId, PrescriptionStatus status);

    byte[] generatePrescriptionPdf(Long prescriptionId);
}
