package com.mediq.service;

import com.mediq.dto.JwtAuthResponse;
import com.mediq.dto.LoginRequest;
import com.mediq.dto.UserProfileResponse;

public interface AuthService {

    JwtAuthResponse login(LoginRequest loginRequest);

    UserProfileResponse getCurrentUserProfile();
}
