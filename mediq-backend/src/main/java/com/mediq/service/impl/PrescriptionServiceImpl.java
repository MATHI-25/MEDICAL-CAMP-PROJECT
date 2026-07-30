package com.mediq.service.impl;

import com.mediq.constants.PrescriptionStatus;
import com.mediq.dto.CreatePrescriptionRequest;
import com.mediq.dto.PrescriptionItemDto;
import com.mediq.dto.PrescriptionResponse;
import com.mediq.entity.*;
import com.mediq.exception.ResourceNotFoundException;
import com.mediq.mapper.PrescriptionMapper;
import com.mediq.repository.CampRepository;
import com.mediq.repository.ConsultationRepository;
import com.mediq.repository.PatientRepository;
import com.mediq.repository.PrescriptionRepository;
import com.mediq.repository.UserRepository;
import com.mediq.service.MedicalHistoryService;
import com.mediq.service.PrescriptionService;
import com.mediq.util.PdfGeneratorUtil;
import com.mediq.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PrescriptionServiceImpl implements PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;
    private final ConsultationRepository consultationRepository;
    private final PatientRepository patientRepository;
    private final CampRepository campRepository;
    private final UserRepository userRepository;
    private final PrescriptionMapper prescriptionMapper;
    private final PdfGeneratorUtil pdfGeneratorUtil;
    private final MedicalHistoryService medicalHistoryService;

    @Override
    @Transactional
    public PrescriptionResponse createPrescription(CreatePrescriptionRequest request) {
        Consultation consultation = consultationRepository.findById(request.getConsultationId())
                .filter(c -> !c.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Consultation", "id", request.getConsultationId()));

        Patient patient = patientRepository.findById(request.getPatientId())
                .filter(p -> !p.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Patient", "id", request.getPatientId()));

        MedicalCamp camp = campRepository.findById(request.getCampId())
                .filter(c -> !c.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("MedicalCamp", "id", request.getCampId()));

        String currentMemberId = SecurityUtils.getCurrentUserMemberId()
                .orElseThrow(() -> new ResourceNotFoundException("Logged in doctor session missing"));

        User doctor = userRepository.findByMemberIdAndIsDeletedFalse(currentMemberId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "memberId", currentMemberId));

        String generatedCode = generatePrescriptionCode(camp.getId());

        Prescription prescription = Prescription.builder()
                .prescriptionCode(generatedCode)
                .consultation(consultation)
                .patient(patient)
                .doctor(doctor)
                .camp(camp)
                .status(PrescriptionStatus.CREATED)
                .generalInstructions(request.getGeneralInstructions())
                .doctorSignature(request.getDoctorSignature() != null ? request.getDoctorSignature() : "Dr. " + doctor.getFullName())
                .build();

        if (request.getItems() != null) {
            for (PrescriptionItemDto itemDto : request.getItems()) {
                PrescriptionItem item = PrescriptionItem.builder()
                        .medicineName(itemDto.getMedicineName())
                        .dosage(itemDto.getDosage())
                        .frequency(itemDto.getFrequency())
                        .duration(itemDto.getDuration())
                        .instructions(itemDto.getInstructions())
                        .quantityPrescribed(itemDto.getQuantityPrescribed())
                        .quantityDispensed(0)
                        .build();
                prescription.addItem(item);
            }
        }

        Prescription savedPrescription = prescriptionRepository.save(prescription);

        // Record prescription event in patient medical history
        medicalHistoryService.recordEvent(
                patient,
                "PRESCRIPTION",
                String.format("Digital Prescription Issued: %s (%d medicines)", generatedCode, savedPrescription.getItems().size()),
                String.format("Prescription code %s prescribed by Dr. %s.", generatedCode, doctor.getFullName()),
                generatedCode,
                doctor.getFullName()
        );

        return prescriptionMapper.toResponse(savedPrescription);
    }

    @Override
    @Transactional(readOnly = true)
    public PrescriptionResponse getPrescriptionById(Long prescriptionId) {
        Prescription prescription = findPrescriptionById(prescriptionId);
        return prescriptionMapper.toResponse(prescription);
    }

    @Override
    @Transactional(readOnly = true)
    public PrescriptionResponse getPrescriptionByCode(String prescriptionCode) {
        Prescription prescription = prescriptionRepository.findByPrescriptionCodeAndIsDeletedFalse(prescriptionCode)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription", "prescriptionCode", prescriptionCode));
        return prescriptionMapper.toResponse(prescription);
    }

    @Override
    @Transactional(readOnly = true)
    public PrescriptionResponse getPrescriptionByConsultationId(Long consultationId) {
        Prescription prescription = prescriptionRepository.findByConsultationIdAndIsDeletedFalse(consultationId)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription", "consultationId", consultationId));
        return prescriptionMapper.toResponse(prescription);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PrescriptionResponse> getPatientPrescriptions(Long patientId) {
        return prescriptionRepository.findByPatientIdAndIsDeletedFalseOrderByCreatedAtDesc(patientId)
                .stream()
                .map(prescriptionMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PrescriptionResponse> getPrescriptionsByStatus(Long campId, PrescriptionStatus status) {
        return prescriptionRepository.findByCampIdAndStatusAndIsDeletedFalseOrderByCreatedAtDesc(campId, status)
                .stream()
                .map(prescriptionMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] generatePrescriptionPdf(Long prescriptionId) {
        Prescription prescription = findPrescriptionById(prescriptionId);
        return pdfGeneratorUtil.generatePrescriptionPdf(prescription);
    }

    private Prescription findPrescriptionById(Long prescriptionId) {
        return prescriptionRepository.findById(prescriptionId)
                .filter(p -> !p.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Prescription", "id", prescriptionId));
    }

    private synchronized String generatePrescriptionCode(Long campId) {
        int year = LocalDate.now().getYear();
        long count = prescriptionRepository.countByCampIdAndIsDeletedFalse(campId) + 1;
        String code = String.format("RX-%d-%04d", year, count);
        while (prescriptionRepository.findByPrescriptionCodeAndIsDeletedFalse(code).isPresent()) {
            count++;
            code = String.format("RX-%d-%04d", year, count);
        }
        return code;
    }
}
