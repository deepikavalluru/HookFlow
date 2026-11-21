package com.hookflow.service;

import com.hookflow.dto.LoginRequest;
import com.hookflow.dto.LoginResponse;
import com.hookflow.entity.User;
import com.hookflow.exception.InvalidCredentialsException;
import com.hookflow.repository.UserRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    void shouldLoginSuccessfully() {
        LoginRequest request = new LoginRequest(
                "deepika@example.com",
                "password123"
        );

        User user = new User();
        user.setEmail("deepika@example.com");
        user.setPassword("encodedPassword");

        when(userRepository.findByEmailIgnoreCase("deepika@example.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches("password123", "encodedPassword"))
                .thenReturn(true);

        when(jwtService.generateToken(user))
                .thenReturn("mock-jwt-token");

        LoginResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("mock-jwt-token", response.accessToken());
        assertEquals("Bearer", response.tokenType());

        verify(userRepository).findByEmailIgnoreCase("deepika@example.com");
        verify(passwordEncoder).matches("password123", "encodedPassword");
        verify(jwtService).generateToken(user);
    }

    @Test
    void shouldRejectLoginWhenEmailDoesNotExist() {
        LoginRequest request = new LoginRequest(
                "unknown@example.com",
                "password123"
        );

        when(userRepository.findByEmailIgnoreCase("unknown@example.com"))
                .thenReturn(Optional.empty());

        assertThrows(
                InvalidCredentialsException.class,
                () -> authService.login(request)
        );

        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtService, never()).generateToken(any(User.class));
    }

    @Test
    void shouldRejectLoginWithIncorrectPassword() {
        LoginRequest request = new LoginRequest(
                "deepika@example.com",
                "wrongPassword"
        );

        User user = new User();
        user.setEmail("deepika@example.com");
        user.setPassword("encodedPassword");

        when(userRepository.findByEmailIgnoreCase("deepika@example.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches("wrongPassword", "encodedPassword"))
                .thenReturn(false);

        assertThrows(
                InvalidCredentialsException.class,
                () -> authService.login(request)
        );

        verify(passwordEncoder).matches("wrongPassword", "encodedPassword");
        verify(jwtService, never()).generateToken(any(User.class));
    }

    @Test
    void shouldTrimEmailDuringLogin() {
        LoginRequest request = new LoginRequest(
                "  deepika@example.com  ",
                "password123"
        );

        User user = new User();
        user.setEmail("deepika@example.com");
        user.setPassword("encodedPassword");

        when(userRepository.findByEmailIgnoreCase("deepika@example.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches("password123", "encodedPassword"))
                .thenReturn(true);

        when(jwtService.generateToken(user))
                .thenReturn("mock-jwt-token");

        LoginResponse response = authService.login(request);

        assertEquals("mock-jwt-token", response.accessToken());

        verify(userRepository).findByEmailIgnoreCase("deepika@example.com");
    }
}