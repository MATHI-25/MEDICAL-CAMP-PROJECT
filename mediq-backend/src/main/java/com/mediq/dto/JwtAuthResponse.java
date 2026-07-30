package com.mediq.dto;

import com.mediq.constants.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JwtAuthResponse {

    private String accessToken;
    @Builder.Default
    private String tokenType = "Bearer";
    private String memberId;
    private String fullName;
    private UserRole role;
    private Long userId;
}
