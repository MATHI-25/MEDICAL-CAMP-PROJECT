package com.mediq.service;

import com.mediq.constants.QueueStatus;
import com.mediq.dto.GenerateTokenRequest;
import com.mediq.dto.QueueDashboardResponse;
import com.mediq.dto.QueueTokenResponse;
import com.mediq.dto.UpdateQueueStatusRequest;

import java.util.List;

public interface QueueService {

    QueueTokenResponse generateToken(GenerateTokenRequest request);

    QueueTokenResponse updateTokenStatus(Long tokenId, UpdateQueueStatusRequest request);

    QueueTokenResponse assignDoctor(Long tokenId, Long doctorId);

    QueueTokenResponse getTokenById(Long tokenId);

    List<QueueTokenResponse> getNurseQueue(Long campId);

    List<QueueTokenResponse> getDoctorQueue(Long campId, Long doctorId);

    List<QueueTokenResponse> getTokensByStatus(Long campId, QueueStatus status);

    QueueDashboardResponse getQueueDashboard(Long campId);

    void cancelToken(Long tokenId);
}
