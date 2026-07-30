package com.mediq.service;

import com.mediq.constants.UserRole;
import com.mediq.dto.CreateUserRequest;
import com.mediq.dto.ResetPasswordRequest;
import com.mediq.dto.UpdateUserRequest;
import com.mediq.dto.UserResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface UserService {

    UserResponse createUser(CreateUserRequest request);

    UserResponse updateUser(Long userId, UpdateUserRequest request);

    UserResponse getUserById(Long userId);

    UserResponse getUserByMemberId(String memberId);

    List<UserResponse> getUsersByRole(UserRole role);

    Page<UserResponse> searchUsers(UserRole role, String keyword, int page, int size, String sortBy, String sortDir);

    UserResponse activateUser(Long userId);

    UserResponse deactivateUser(Long userId);

    void deleteUser(Long userId);

    void resetPassword(Long userId, ResetPasswordRequest request);
}
