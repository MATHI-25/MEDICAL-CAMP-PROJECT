package com.mediq.controller;

import com.mediq.dto.ApiResponse;
import com.mediq.dto.JwtAuthResponse;
import com.mediq.dto.LoginRequest;
import com.mediq.dto.UserProfileResponse;
import com.mediq.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication APIs for User Login, Role Verification, and Session Profile")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Authenticate user and issue JWT token", description = "Validates Member ID, Password, and requested Role. Returns Bearer JWT token.")
    public ResponseEntity<ApiResponse<JwtAuthResponse>> login(@Valid @RequestBody LoginRequest loginRequest) {
        JwtAuthResponse response = authService.login(loginRequest);
        return ResponseEntity.ok(ApiResponse.success(response, "Login successful"));
    }

    @GetMapping("/me")
    @Operation(summary = "Get current authenticated user profile", description = "Retrieves logged-in user profile details based on JWT context.")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getCurrentUser() {
        UserProfileResponse profile = authService.getCurrentUserProfile();
        return ResponseEntity.ok(ApiResponse.success(profile, "User profile retrieved successfully"));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout user session", description = "Invalidates active user session on client side.")
    public ResponseEntity<ApiResponse<Void>> logout() {
        return ResponseEntity.ok(ApiResponse.success("Logout successful"));
    }
}
