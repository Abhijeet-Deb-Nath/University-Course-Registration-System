package com.example.universitycourseregistrationsystem.service;

import com.example.universitycourseregistrationsystem.domain.Role;
import com.example.universitycourseregistrationsystem.domain.User;
import com.example.universitycourseregistrationsystem.dto.RegisterRequest;
import com.example.universitycourseregistrationsystem.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit Test for UserService
 *
 * Demonstrates unit testing with mocked dependencies.
 * Tests user registration business logic in isolation.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    /**
     * Tests successful user registration - verifies the service correctly
     * encodes password and saves user when username is unique.
     */
    @Test
    void register_WhenUsernameIsUnique_ShouldCreateUser() {
        // Given
        RegisterRequest request = new RegisterRequest("newuser", "password123", Role.STUDENT);
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });

        // When
        User result = userService.register(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("newuser");
        assertThat(result.getPasswordHash()).isEqualTo("encodedPassword");
        assertThat(result.getRole()).isEqualTo(Role.STUDENT);

        verify(userRepository).existsByUsername("newuser");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
    }

    /**
     * Tests duplicate username handling - verifies the service throws
     * an exception when username already exists.
     */
    @Test
    void register_WhenUsernameExists_ShouldThrowConflict() {
        // Given
        RegisterRequest request = new RegisterRequest("existinguser", "password123", Role.STUDENT);
        when(userRepository.existsByUsername("existinguser")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Username already exists");

        verify(userRepository).existsByUsername("existinguser");
        verify(userRepository, never()).save(any(User.class));
    }
}
