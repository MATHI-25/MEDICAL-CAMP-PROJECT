package com.mediq.mapper;

import com.mediq.dto.QueueTokenResponse;
import com.mediq.entity.QueueToken;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QueueMapper {

    private final PatientMapper patientMapper;
    private final UserMapper userMapper;

    public QueueTokenResponse toResponse(QueueToken token) {
        if (token == null) {
            return null;
        }

        return QueueTokenResponse.builder()
                .id(token.getId())
                .tokenNumber(token.getTokenNumber())
                .sequenceNumber(token.getSequenceNumber())
                .patient(patientMapper.toResponse(token.getPatient()))
                .campId(token.getCamp() != null ? token.getCamp().getId() : null)
                .campTitle(token.getCamp() != null ? token.getCamp().getTitle() : null)
                .assignedDoctor(userMapper.toResponse(token.getAssignedDoctor()))
                .status(token.getStatus())
                .estimatedWaitMinutes(token.getEstimatedWaitMinutes())
                .createdAt(token.getCreatedAt())
                .updatedAt(token.getUpdatedAt())
                .build();
    }
}
