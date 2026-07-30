package com.mediq.dto;

import com.mediq.constants.ReferralStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateReferralStatusRequest {

    @NotNull(message = "Referral status is required")
    private ReferralStatus status;

    private String remarks;
}
