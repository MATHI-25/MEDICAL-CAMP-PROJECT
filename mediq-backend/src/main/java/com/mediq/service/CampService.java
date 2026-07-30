package com.mediq.service;

import com.mediq.constants.CampStatus;
import com.mediq.dto.AssignStaffRequest;
import com.mediq.dto.CampResponse;
import com.mediq.dto.CreateCampRequest;
import com.mediq.dto.UpdateCampRequest;
import org.springframework.data.domain.Page;

import java.util.List;

public interface CampService {

    CampResponse createCamp(CreateCampRequest request);

    CampResponse updateCamp(Long campId, UpdateCampRequest request);

    CampResponse getCampById(Long campId);

    CampResponse getCampByCode(String campCode);

    CampResponse assignStaff(Long campId, AssignStaffRequest request);

    CampResponse updateCampStatus(Long campId, CampStatus status);

    List<CampResponse> getCampsByStatus(CampStatus status);

    Page<CampResponse> searchCamps(CampStatus status, String keyword, int page, int size, String sortBy, String sortDir);

    void deleteCamp(Long campId);
}
