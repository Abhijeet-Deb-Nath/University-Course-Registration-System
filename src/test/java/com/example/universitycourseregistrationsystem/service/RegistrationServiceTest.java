package com.example.universitycourseregistrationsystem.service;

import com.example.universitycourseregistrationsystem.domain.Course;
import com.example.universitycourseregistrationsystem.domain.Registration;
import com.example.universitycourseregistrationsystem.domain.Role;
import com.example.universitycourseregistrationsystem.domain.User;
import com.example.universitycourseregistrationsystem.repository.RegistrationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit Test for RegistrationService
 *
 * Demonstrates unit testing with mocked dependencies.
 * Tests course registration business logic in isolation.
 */
@ExtendWith(MockitoExtension.class)
class RegistrationServiceTest {

    @Mock
    private RegistrationRepository registrationRepository;

    @Mock
    private CourseService courseService;

    @Mock
    private UserService userService;

    @InjectMocks
    private RegistrationService registrationService;

    private User student;
    private User teacher;
    private Course course;

    @BeforeEach
    void setUp() {
        student = new User();
        student.setId(1L);
        student.setUsername("student1");
        student.setRole(Role.STUDENT);

        teacher = new User();
        teacher.setId(2L);
        teacher.setUsername("teacher1");
        teacher.setRole(Role.TEACHER);

        course = new Course();
        course.setId(1L);
        course.setCourseNo("CS101");
        course.setCourseName("Introduction to Computer Science");
        course.setTeacher(teacher);
    }

    /**
     * Tests successful registration - verifies the service creates
     * a registration when student is not already registered.
     */
    @Test
    void register_WhenNotAlreadyRegistered_ShouldCreateRegistration() {
        // Given
        when(userService.requireRole(Role.STUDENT)).thenReturn(student);
        when(courseService.getCourseOrThrow(1L)).thenReturn(course);
        when(registrationRepository.existsByStudentIdAndCourseId(1L, 1L)).thenReturn(false);
        when(registrationRepository.save(any(Registration.class))).thenAnswer(invocation -> {
            Registration reg = invocation.getArgument(0);
            reg.setId(1L);
            return reg;
        });

        // When
        Registration result = registrationService.register(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStudent()).isEqualTo(student);
        assertThat(result.getCourse()).isEqualTo(course);
        verify(registrationRepository).save(any(Registration.class));
    }

    /**
     * Tests duplicate registration handling - verifies the service
     * throws an exception when student is already registered.
     */
    @Test
    void register_WhenAlreadyRegistered_ShouldThrowConflict() {
        // Given
        when(userService.requireRole(Role.STUDENT)).thenReturn(student);
        when(courseService.getCourseOrThrow(1L)).thenReturn(course);
        when(registrationRepository.existsByStudentIdAndCourseId(1L, 1L)).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> registrationService.register(1L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Already registered");

        verify(registrationRepository, never()).save(any(Registration.class));
    }
}
