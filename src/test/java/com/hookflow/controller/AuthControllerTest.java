package com.hookflow.controller;

import com.hookflow.dto.LoginRequest;
import com.hookflow.dto.LoginResponse;
import com.hookflow.dto.RegisterRequest;
import com.hookflow.entity.User;
import com.hookflow.service.UserService;
import com.hookflow.service.AuthService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @Test
    void shouldRegisterUser() {
        RegisterRequest request = mock(RegisterRequest.class);

        LocalDateTime createdAt = LocalDateTime.now();

        User user = mock(User.class);

        when(userService.registerUser(request)).thenReturn(user);

        when(user.getId()).thenReturn(1L);
        when(user.getName()).thenReturn("Deepika");
        when(user.getEmail()).thenReturn("deepika@example.com");
        when(user.getCreatedAt()).thenReturn(createdAt);

        var response = authController.register(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());

        assertEquals(1L, response.getBody().id());
        assertEquals("Deepika", response.getBody().name());
        assertEquals("deepika@example.com", response.getBody().email());
        assertEquals(createdAt, response.getBody().createdAt());

        verify(userService).registerUser(request);
    }

    @Test
    void shouldLoginUser() {
        LoginRequest request = mock(LoginRequest.class);
        LoginResponse expectedResponse = mock(LoginResponse.class);

        when(authService.login(request)).thenReturn(expectedResponse);

        var response = authController.login(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResponse, response.getBody());

        verify(authService).login(request);
    }

    @Test
    void shouldReturnCurrentUser() {
        Long userId = 1L;

        Authentication authentication = mock(Authentication.class);

        User user = mock(User.class);

        LocalDateTime createdAt = LocalDateTime.now();

        when(authentication.getPrincipal()).thenReturn(userId);
        when(userService.getUserById(userId)).thenReturn(user);

        when(user.getId()).thenReturn(userId);
        when(user.getName()).thenReturn("Deepika");
        when(user.getEmail()).thenReturn("deepika@example.com");
        when(user.getCreatedAt()).thenReturn(createdAt);

        var response = authController.getCurrentUser(authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        assertEquals(userId, response.getBody().id());
        assertEquals("Deepika", response.getBody().name());
        assertEquals("deepika@example.com", response.getBody().email());
        assertEquals(createdAt, response.getBody().createdAt());

        verify(userService).getUserById(userId);
    }
}