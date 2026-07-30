package com.mediq.service;

import com.mediq.dto.*;

import java.util.List;

public interface ReportService {

    CampAnalyticsResponse getCampAnalytics(Long campId);

    List<DoctorReportResponse> getDoctorWorkloadReport(Long campId);

    List<MedicineReportResponse> getMedicineConsumptionReport();

    ReferralReportResponse getReferralReport(Long campId);

    GlobalSearchResultResponse globalSearch(String query);
}
