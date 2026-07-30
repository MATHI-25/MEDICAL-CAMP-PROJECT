package com.mediq.service.impl;

import com.mediq.dto.PatientResponse;
import com.mediq.dto.RegisterPatientRequest;
import com.mediq.dto.UpdatePatientRequest;
import com.mediq.entity.MedicalCamp;
import com.mediq.entity.Patient;
import com.mediq.exception.ResourceNotFoundException;
import com.mediq.mapper.PatientMapper;
import com.mediq.repository.CampRepository;
import com.mediq.repository.PatientRepository;
import com.mediq.service.MedicalHistoryService;
import com.mediq.service.PatientService;
import com.mediq.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PatientServiceImpl implements PatientService {

    private final PatientRepository patientRepository;
    private final CampRepository campRepository;
    private final PatientMapper patientMapper;
    private final MedicalHistoryService medicalHistoryService;

    @Override
    @Transactional
    public PatientResponse registerPatient(RegisterPatientRequest request) {
        MedicalCamp camp = campRepository.findById(request.getRegisteredCampId())
                .filter(c -> !c.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("MedicalCamp", "id", request.getRegisteredCampId()));

        String generatedPatientId = generatePatientId(camp.getId());

        Patient patient = Patient.builder()
                .patientId(generatedPatientId)
                .fullName(request.getFullName())
                .age(request.getAge())
                .gender(request.getGender())
                .bloodGroup(request.getBloodGroup())
                .phone(request.getPhone())
                .address(request.getAddress())
                .emergencyContact(request.getEmergencyContact())
                .allergies(request.getAllergies())
                .chronicDiseases(request.getChronicDiseases())
                .registeredCamp(camp)
                .build();

        Patient savedPatient = patientRepository.save(patient);

        String currentUser = SecurityUtils.getCurrentUserMemberId().orElse("VOLUNTEER");
        medicalHistoryService.recordEvent(
                savedPatient,
                "REGISTRATION",
                "Patient registered at camp: " + camp.getTitle(),
                String.format("Initial registration by %s at %s.", currentUser, camp.getVenue()),
                savedPatient.getPatientId(),
                currentUser
        );

        return patientMapper.toResponse(savedPatient);
    }

    @Override
    @Transactional
    public PatientResponse updatePatient(Long patientId, UpdatePatientRequest request) {
        Patient patient = findPatientById(patientId);

        patient.setFullName(request.getFullName());
        patient.setAge(request.getAge());
        patient.setGender(request.getGender());
        patient.setBloodGroup(request.getBloodGroup());
        patient.setPhone(request.getPhone());
        patient.setAddress(request.getAddress());
        patient.setEmergencyContact(request.getEmergencyContact());
        patient.setAllergies(request.getAllergies());
        patient.setChronicDiseases(request.getChronicDiseases());

        Patient updatedPatient = patientRepository.save(patient);
        return patientMapper.toResponse(updatedPatient);
    }

    @Override
    @Transactional(readOnly = true)
    public PatientResponse getPatientById(Long patientId) {
        return patientMapper.toResponse(findPatientById(patientId));
    }

    @Override
    @Transactional(readOnly = true)
    public PatientResponse getPatientByPatientId(String patientId) {
        Patient patient = patientRepository.findByPatientIdAndIsDeletedFalse(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", "patientId", patientId));
        return patientMapper.toResponse(patient);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PatientResponse> getPatientsByPhone(String phone) {
        return patientRepository.findByPhoneAndIsDeletedFalse(phone)
                .stream()
                .map(patientMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PatientResponse> searchPatients(Long campId, String keyword, int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        return patientRepository.searchPatients(campId, keyword, pageable)
                .map(patientMapper::toResponse);
    }

    @Override
    @Transactional
    public void deletePatient(Long patientId) {
        Patient patient = findPatientById(patientId);
        patient.setDeleted(true);
        patientRepository.save(patient);
    }

    private Patient findPatientById(Long patientId) {
        return patientRepository.findById(patientId)
                .filter(p -> !p.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Patient", "id", patientId));
    }

    private synchronized String generatePatientId(Long campId) {
        int currentYear = LocalDate.now().getYear();
        long campPatientCount = patientRepository.countByRegisteredCampIdAndIsDeletedFalse(campId) + 1;
        return String.format("PAT-%d-%04d", currentYear, campPatientCount);
    }
}
