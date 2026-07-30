package com.mediq.service.impl;

import com.mediq.constants.QueueStatus;
import com.mediq.constants.UserRole;
import com.mediq.dto.GenerateTokenRequest;
import com.mediq.dto.QueueDashboardResponse;
import com.mediq.dto.QueueTokenResponse;
import com.mediq.dto.UpdateQueueStatusRequest;
import com.mediq.entity.MedicalCamp;
import com.mediq.entity.Patient;
import com.mediq.entity.QueueToken;
import com.mediq.entity.User;
import com.mediq.exception.BadRequestException;
import com.mediq.exception.ResourceNotFoundException;
import com.mediq.mapper.QueueMapper;
import com.mediq.repository.CampRepository;
import com.mediq.repository.PatientRepository;
import com.mediq.repository.QueueTokenRepository;
import com.mediq.repository.UserRepository;
import com.mediq.service.QueueService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QueueServiceImpl implements QueueService {

    private final QueueTokenRepository queueTokenRepository;
    private final PatientRepository patientRepository;
    private final CampRepository campRepository;
    private final UserRepository userRepository;
    private final QueueMapper queueMapper;

    @Override
    @Transactional
    public QueueTokenResponse generateToken(GenerateTokenRequest request) {
        Patient patient = patientRepository.findById(request.getPatientId())
                .filter(p -> !p.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Patient", "id", request.getPatientId()));

        MedicalCamp camp = campRepository.findById(request.getCampId())
                .filter(c -> !c.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("MedicalCamp", "id", request.getCampId()));

        User doctor = null;
        if (request.getAssignedDoctorId() != null) {
            doctor = userRepository.findById(request.getAssignedDoctorId())
                    .filter(u -> !u.isDeleted() && u.getRole() == UserRole.DOCTOR)
                    .orElse(null);
        }

        int nextSequence = queueTokenRepository.findTopByCampIdAndIsDeletedFalseOrderBySequenceNumberDesc(camp.getId())
                .map(t -> t.getSequenceNumber() + 1)
                .orElse(1);

        String tokenNumber = String.format("TKN-%03d", nextSequence);
        long waitingCount = queueTokenRepository.countByCampIdAndStatusAndIsDeletedFalse(camp.getId(), QueueStatus.WAITING);
        int estWaitMins = (int) (waitingCount * 10) + 10;

        QueueToken token = QueueToken.builder()
                .tokenNumber(tokenNumber)
                .sequenceNumber(nextSequence)
                .patient(patient)
                .camp(camp)
                .assignedDoctor(doctor)
                .status(QueueStatus.WAITING)
                .estimatedWaitMinutes(estWaitMins)
                .build();

        QueueToken savedToken = queueTokenRepository.save(token);
        return queueMapper.toResponse(savedToken);
    }

    @Override
    @Transactional
    public QueueTokenResponse updateTokenStatus(Long tokenId, UpdateQueueStatusRequest request) {
        QueueToken token = findTokenById(tokenId);
        token.setStatus(request.getStatus());

        if (request.getAssignedDoctorId() != null) {
            User doctor = userRepository.findById(request.getAssignedDoctorId())
                    .filter(u -> !u.isDeleted() && u.getRole() == UserRole.DOCTOR)
                    .orElseThrow(() -> new BadRequestException("Invalid doctor ID provided"));
            token.setAssignedDoctor(doctor);
        }

        return queueMapper.toResponse(queueTokenRepository.save(token));
    }

    @Override
    @Transactional
    public QueueTokenResponse assignDoctor(Long tokenId, Long doctorId) {
        QueueToken token = findTokenById(tokenId);
        User doctor = userRepository.findById(doctorId)
                .filter(u -> !u.isDeleted() && u.getRole() == UserRole.DOCTOR)
                .orElseThrow(() -> new BadRequestException("Invalid doctor ID provided"));

        token.setAssignedDoctor(doctor);
        if (token.getStatus() == QueueStatus.WAITING || token.getStatus() == QueueStatus.IN_VITALS) {
            token.setStatus(QueueStatus.WAITING_FOR_DOCTOR);
        }

        return queueMapper.toResponse(queueTokenRepository.save(token));
    }

    @Override
    @Transactional(readOnly = true)
    public QueueTokenResponse getTokenById(Long tokenId) {
        return queueMapper.toResponse(findTokenById(tokenId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<QueueTokenResponse> getNurseQueue(Long campId) {
        return queueTokenRepository.findNurseQueueTokens(campId)
                .stream()
                .map(queueMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<QueueTokenResponse> getDoctorQueue(Long campId, Long doctorId) {
        return queueTokenRepository.findDoctorQueueTokens(campId, doctorId)
                .stream()
                .map(queueMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<QueueTokenResponse> getTokensByStatus(Long campId, QueueStatus status) {
        return queueTokenRepository.findByCampIdAndStatusAndIsDeletedFalseOrderBySequenceNumberAsc(campId, status)
                .stream()
                .map(queueMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public QueueDashboardResponse getQueueDashboard(Long campId) {
        MedicalCamp camp = campRepository.findById(campId)
                .filter(c -> !c.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("MedicalCamp", "id", campId));

        long total = queueTokenRepository.countByCampIdAndIsDeletedFalse(campId);
        long waiting = queueTokenRepository.countByCampIdAndStatusAndIsDeletedFalse(campId, QueueStatus.WAITING);
        long vitals = queueTokenRepository.countByCampIdAndStatusAndIsDeletedFalse(campId, QueueStatus.IN_VITALS);
        long waitingDoc = queueTokenRepository.countByCampIdAndStatusAndIsDeletedFalse(campId, QueueStatus.WAITING_FOR_DOCTOR);
        long inConsult = queueTokenRepository.countByCampIdAndStatusAndIsDeletedFalse(campId, QueueStatus.IN_CONSULTATION);
        long pharmacy = queueTokenRepository.countByCampIdAndStatusAndIsDeletedFalse(campId, QueueStatus.SENT_TO_PHARMACY);
        long referred = queueTokenRepository.countByCampIdAndStatusAndIsDeletedFalse(campId, QueueStatus.REFERRED_TO_HOSPITAL);
        long completed = queueTokenRepository.countByCampIdAndStatusAndIsDeletedFalse(campId, QueueStatus.COMPLETED);

        int avgWaitMins = (int) ((waiting + vitals + waitingDoc) * 8);

        return QueueDashboardResponse.builder()
                .campId(camp.getId())
                .campTitle(camp.getTitle())
                .totalTokens(total)
                .waitingCount(waiting)
                .inVitalsCount(vitals)
                .waitingForDoctorCount(waitingDoc)
                .inConsultationCount(inConsult)
                .pharmacyCount(pharmacy)
                .referredCount(referred)
                .completedCount(completed)
                .estimatedAverageWaitMinutes(avgWaitMins)
                .build();
    }

    @Override
    @Transactional
    public void cancelToken(Long tokenId) {
        QueueToken token = findTokenById(tokenId);
        token.setStatus(QueueStatus.CANCELLED);
        queueTokenRepository.save(token);
    }

    private QueueToken findTokenById(Long tokenId) {
        return queueTokenRepository.findById(tokenId)
                .filter(q -> !q.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("QueueToken", "id", tokenId));
    }
}
