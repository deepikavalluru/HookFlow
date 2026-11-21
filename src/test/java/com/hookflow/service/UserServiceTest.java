package com.hookflow.service;

import com.hookflow.dto.RegisterRequest;
import com.hookflow.entity.User;
import com.hookflow.exception.EmailAlreadyExistsException;
import com.hookflow.repository.UserRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void shouldRegisterUserSuccessfully() {
        RegisterRequest request = new RegisterRequest(
                "Deepika",
                "Deepika@Example.com",
                "password123"
        );

        when(userRepository.existsByEmailIgnoreCase("deepika@example.com"))
                .thenReturn(false);

        when(passwordEncoder.encode("password123"))
                .thenReturn("encodedPassword");

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        User savedUser = userService.registerUser(request);

        assertNotNull(savedUser);
        assertEquals("Deepika", savedUser.getName());
        assertEquals("deepika@example.com", savedUser.getEmail());
        assertEquals("encodedPassword", savedUser.getPassword());

        verify(userRepository).existsByEmailIgnoreCase("deepika@example.com");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldRejectDuplicateEmail() {
        RegisterRequest request = new RegisterRequest(
                "Deepika",
                "deepika@example.com",
                "password123"
        );

        when(userRepository.existsByEmailIgnoreCase("deepika@example.com"))
                .thenReturn(true);

        assertThrows(
                EmailAlreadyExistsException.class,
                () -> userService.registerUser(request)
        );

        verify(userRepository).existsByEmailIgnoreCase("deepika@example.com");
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldNormalizeNameAndEmailDuringRegistration() {
        RegisterRequest request = new RegisterRequest(
                "  Deepika  ",
                "  Deepika@Example.com  ",
                "password123"
        );

        when(userRepository.existsByEmailIgnoreCase("deepika@example.com"))
                .thenReturn(false);

        when(passwordEncoder.encode("password123"))
                .thenReturn("encodedPassword");

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        User savedUser = userService.registerUser(request);

        assertEquals("Deepika", savedUser.getName());
        assertEquals("deepika@example.com", savedUser.getEmail());

        verify(userRepository).existsByEmailIgnoreCase("deepika@example.com");
    }

    @Test
    void shouldStoreEncodedPasswordInsteadOfRawPassword() {
        RegisterRequest request = new RegisterRequest(
                "Deepika",
                "deepika@example.com",
                "password123"
        );

        when(userRepository.existsByEmailIgnoreCase("deepika@example.com"))
                .thenReturn(false);

        when(passwordEncoder.encode("password123"))
                .thenReturn("encodedPassword");

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> {
                    User user = invocation.getArgument(0);

                    assertNotEquals("password123", user.getPassword());
                    assertEquals("encodedPassword", user.getPassword());

                    return user;
                });

        userService.registerUser(request);

        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
    }
}