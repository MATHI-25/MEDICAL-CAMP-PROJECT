package com.mediq.controller;

import com.mediq.constants.UserRole;
import com.mediq.dto.*;
import com.mediq.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "System Administration APIs for User Lifecycle, Roles, Status Toggle, and Password Resets")
public class UserController {

    private final UserService userService;

    @PostMapping
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Operation(summary = "Create a new user account", description = "Creates a new staff account (Organizer, Doctor, Nurse, Pharmacy, Volunteer) with specified Member ID and role.")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(@Valid @RequestBody CreateUserRequest request) {
        UserResponse response = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "User created successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Operation(summary = "Update user details", description = "Updates profile attributes, role, specialization, or department of an existing user.")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request) {
        UserResponse response = userService.updateUser(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "User updated successfully"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID", description = "Retrieves user details by primary database ID.")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
        UserResponse response = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success(response, "User fetched successfully"));
    }

    @GetMapping("/member/{memberId}")
    @Operation(summary = "Get user by Member ID", description = "Retrieves user details by system Member ID (e.g. MC-DOC-001).")
    public ResponseEntity<ApiResponse<UserResponse>> getUserByMemberId(@PathVariable String memberId) {
        UserResponse response = userService.getUserByMemberId(memberId);
        return ResponseEntity.ok(ApiResponse.success(response, "User fetched successfully"));
    }

    @GetMapping("/role/{role}")
    @Operation(summary = "Get active users by role", description = "Retrieves list of active staff members filtered by system role.")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getUsersByRole(@PathVariable UserRole role) {
        List<UserResponse> responses = userService.getUsersByRole(role);
        return ResponseEntity.ok(ApiResponse.success(responses, "Users fetched successfully"));
    }

    @GetMapping
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('ORGANIZER')")
    @Operation(summary = "Search users with pagination", description = "Filters users by role, search keyword (name, member ID, phone), and pagination parameters.")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> searchUsers(
            @RequestParam(required = false) UserRole role,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Page<UserResponse> responses = userService.searchUsers(role, keyword, page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success(responses, "Users searched successfully"));
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Operation(summary = "Activate user account", description = "Enables a deactivated user account.")
    public ResponseEntity<ApiResponse<UserResponse>> activateUser(@PathVariable Long id) {
        UserResponse response = userService.activateUser(id);
        return ResponseEntity.ok(ApiResponse.success(response, "User account activated successfully"));
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Operation(summary = "Deactivate user account", description = "Disables an active user account preventing further logins.")
    public ResponseEntity<ApiResponse<UserResponse>> deactivateUser(@PathVariable Long id) {
        UserResponse response = userService.deactivateUser(id);
        return ResponseEntity.ok(ApiResponse.success(response, "User account deactivated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Operation(summary = "Soft delete user account", description = "Marks user record as deleted in system audit.")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success("User account deleted successfully"));
    }

    @PatchMapping("/{id}/reset-password")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Operation(summary = "Reset user password", description = "Resets user password with BCrypt hash.")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @PathVariable Long id,
            @Valid @RequestBody ResetPasswordRequest request) {
        userService.resetPassword(id, request);
        return ResponseEntity.ok(ApiResponse.success("Password reset successfully"));
    }
}
