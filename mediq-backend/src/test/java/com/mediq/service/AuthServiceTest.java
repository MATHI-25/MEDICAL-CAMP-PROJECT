package com.mediq.service;

import com.mediq.constants.UserRole;
import com.mediq.dto.JwtAuthResponse;
import com.mediq.dto.LoginRequest;
import com.mediq.entity.User;
import com.mediq.repository.UserRepository;
import com.mediq.security.CustomUserDetails;
import com.mediq.security.JwtTokenProvider;
import com.mediq.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthServiceImpl authService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = User.builder()
                .id(1L)
                .memberId("MC-ADM-001")
                .fullName("System Administrator")
                .role(UserRole.SYSTEM_ADMIN)
                .isActive(true)
                .build();
    }

    @Test
    void testLoginSuccess() {
        LoginRequest request = LoginRequest.builder()
                .memberId("MC-ADM-001")
                .password("Camp@2026")
                .role(UserRole.SYSTEM_ADMIN)
                .build();

        Authentication auth = mock(Authentication.class);
        CustomUserDetails userDetails = new CustomUserDetails(sampleUser);

        when(userRepository.findByMemberIdAndIsDeletedFalse("MC-ADM-001")).thenReturn(Optional.of(sampleUser));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);
        when(auth.getPrincipal()).thenReturn(userDetails);
        when(jwtTokenProvider.generateToken(auth)).thenReturn("mock-jwt-token-xyz");

        JwtAuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("mock-jwt-token-xyz", response.getAccessToken());
        assertEquals("MC-ADM-001", response.getMemberId());
        assertEquals(UserRole.SYSTEM_ADMIN, response.getRole());
        verify(authenticationManager, times(1)).authenticate(any());
    }
}
