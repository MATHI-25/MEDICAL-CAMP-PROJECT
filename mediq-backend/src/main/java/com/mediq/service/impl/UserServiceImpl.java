package com.mediq.service.impl;

import com.mediq.constants.UserRole;
import com.mediq.dto.CreateUserRequest;
import com.mediq.dto.ResetPasswordRequest;
import com.mediq.dto.UpdateUserRequest;
import com.mediq.dto.UserResponse;
import com.mediq.entity.User;
import com.mediq.exception.BadRequestException;
import com.mediq.exception.ResourceNotFoundException;
import com.mediq.mapper.UserMapper;
import com.mediq.repository.UserRepository;
import com.mediq.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByMemberIdAndIsDeletedFalse(request.getMemberId())) {
            throw new BadRequestException("Member ID '" + request.getMemberId() + "' already exists");
        }

        if (request.getEmail() != null && !request.getEmail().isBlank() &&
                userRepository.existsByEmailAndIsDeletedFalse(request.getEmail())) {
            throw new BadRequestException("Email address '" + request.getEmail() + "' already exists");
        }

        User user = User.builder()
                .memberId(request.getMemberId())
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .role(request.getRole())
                .password(passwordEncoder.encode(request.getPassword()))
                .specialization(request.getSpecialization())
                .department(request.getDepartment())
                .isActive(true)
                .build();

        User savedUser = userRepository.save(user);
        return userMapper.toResponse(savedUser);
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long userId, UpdateUserRequest request) {
        User user = findUserById(userId);

        if (request.getEmail() != null && !request.getEmail().isBlank() &&
                !request.getEmail().equalsIgnoreCase(user.getEmail()) &&
                userRepository.existsByEmailAndIsDeletedFalse(request.getEmail())) {
            throw new BadRequestException("Email address '" + request.getEmail() + "' already in use by another user");
        }

        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setRole(request.getRole());
        user.setSpecialization(request.getSpecialization());
        user.setDepartment(request.getDepartment());

        User updatedUser = userRepository.save(user);
        return userMapper.toResponse(updatedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long userId) {
        return userMapper.toResponse(findUserById(userId));
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserByMemberId(String memberId) {
        User user = userRepository.findByMemberIdAndIsDeletedFalse(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "memberId", memberId));
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getUsersByRole(UserRole role) {
        return userRepository.findByRoleAndIsActiveTrueAndIsDeletedFalse(role)
                .stream()
                .map(userMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> searchUsers(UserRole role, String keyword, int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        return userRepository.searchUsers(role, keyword, pageable)
                .map(userMapper::toResponse);
    }

    @Override
    @Transactional
    public UserResponse activateUser(Long userId) {
        User user = findUserById(userId);
        user.setActive(true);
        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserResponse deactivateUser(Long userId) {
        User user = findUserById(userId);
        user.setActive(false);
        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        User user = findUserById(userId);
        user.setDeleted(true);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void resetPassword(Long userId, ResetPasswordRequest request) {
        User user = findUserById(userId);
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .filter(u -> !u.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
    }
}
