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
public class UserProfileResponse {

    private Long id;
    private String memberId;
    private String fullName;
    private String email;
    private String phone;
    private UserRole role;
    private String specialization;
    private String department;
    private boolean isActive;
}
