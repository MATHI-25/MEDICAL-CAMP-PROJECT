package com.mediq.service.impl;

import com.mediq.dto.JwtAuthResponse;
import com.mediq.dto.LoginRequest;
import com.mediq.dto.UserProfileResponse;
import com.mediq.entity.User;
import com.mediq.exception.BadRequestException;
import com.mediq.exception.ResourceNotFoundException;
import com.mediq.exception.UnauthorizedException;
import com.mediq.repository.UserRepository;
import com.mediq.security.CustomUserDetails;
import com.mediq.security.JwtTokenProvider;
import com.mediq.service.AuthService;
import com.mediq.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtTokenProvider tokenProvider;

    @Override
    @Transactional(readOnly = true)
    public JwtAuthResponse login(LoginRequest loginRequest) {
        User user = userRepository.findByMemberIdAndIsDeletedFalse(loginRequest.getMemberId())
                .orElseThrow(() -> new BadCredentialsException("Invalid Member ID or Password"));

        if (!user.isActive()) {
            throw new BadRequestException("User account is deactivated. Please contact System Administrator.");
        }

        if (user.getRole() != loginRequest.getRole()) {
            throw new UnauthorizedException(String.format("User %s does not hold requested role %s", loginRequest.getMemberId(), loginRequest.getRole()));
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getMemberId(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwtToken = tokenProvider.generateToken(authentication);

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        return JwtAuthResponse.builder()
                .accessToken(jwtToken)
                .tokenType("Bearer")
                .memberId(userDetails.getUser().getMemberId())
                .fullName(userDetails.getUser().getFullName())
                .role(userDetails.getUser().getRole())
                .userId(userDetails.getUser().getId())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getCurrentUserProfile() {
        String memberId = SecurityUtils.getCurrentUserMemberId()
                .orElseThrow(() -> new UnauthorizedException("User session expired or unauthenticated"));

        User user = userRepository.findByMemberIdAndIsDeletedFalse(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "memberId", memberId));

        return UserProfileResponse.builder()
                .id(user.getId())
                .memberId(user.getMemberId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .specialization(user.getSpecialization())
                .department(user.getDepartment())
                .isActive(user.isActive())
                .build();
    }
}
