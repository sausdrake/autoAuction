package com.example.autoauction.auth.application;

import com.example.autoauction.auth.infrastructure.security.JwtService;
import com.example.autoauction.user.domain.User;
import com.example.autoauction.user.domain.port.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserDetailsService userDetailsService;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private AuthService.RegisterRequest registerRequest;
    private AuthService.LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        testUser = new User(
                1L,
                "testuser",
                "test@example.com",
                "encodedPassword",
                true,
                Set.of()
        );

        registerRequest = new AuthService.RegisterRequest(
                "newuser",
                "new@example.com",
                "password123"
        );

        loginRequest = new AuthService.LoginRequest(
                "testuser",
                "password123"
        );
    }

    @Test
    void register_Success() {
        // given
        when(userRepository.findByUsername(registerRequest.username())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(registerRequest.email())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(registerRequest.password())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserDetails userDetails = mock(UserDetails.class);
        when(userDetailsService.loadUserByUsername(testUser.getUsername())).thenReturn(userDetails);
        when(jwtService.generateToken(userDetails)).thenReturn("jwt-token");
        when(jwtService.generateRefreshToken(userDetails)).thenReturn("refresh-token");

        // when
        AuthService.AuthResponse response = authService.register(registerRequest);

        // then
        assertNotNull(response);
        assertEquals("jwt-token", response.accessToken());
        assertEquals("refresh-token", response.refreshToken());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void register_UsernameAlreadyExists_ThrowsException() {
        // given
        when(userRepository.findByUsername(registerRequest.username())).thenReturn(Optional.of(testUser));

        // when & then
        assertThrows(RuntimeException.class, () -> authService.register(registerRequest));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void authenticate_Success() {
        // given
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("testuser");
        when(userDetailsService.loadUserByUsername(loginRequest.username())).thenReturn(userDetails);
        when(jwtService.generateToken(userDetails)).thenReturn("jwt-token");
        when(jwtService.generateRefreshToken(userDetails)).thenReturn("refresh-token");

        // when
        AuthService.AuthResponse response = authService.authenticate(loginRequest);

        // then
        assertNotNull(response);
        assertEquals("jwt-token", response.accessToken());
        assertEquals("refresh-token", response.refreshToken());
        verify(authenticationManager, times(1)).authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.username(), loginRequest.password())
        );
    }
}