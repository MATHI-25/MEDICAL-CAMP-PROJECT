package com.mediq.service;

import com.mediq.dto.PatientResponse;
import com.mediq.dto.RegisterPatientRequest;
import com.mediq.dto.UpdatePatientRequest;
import org.springframework.data.domain.Page;

import java.util.List;

public interface PatientService {

    PatientResponse registerPatient(RegisterPatientRequest request);

    PatientResponse updatePatient(Long patientId, UpdatePatientRequest request);

    PatientResponse getPatientById(Long patientId);

    PatientResponse getPatientByPatientId(String patientId);

    List<PatientResponse> getPatientsByPhone(String phone);

    Page<PatientResponse> searchPatients(Long campId, String keyword, int page, int size, String sortBy, String sortDir);

    void deletePatient(Long patientId);
}
