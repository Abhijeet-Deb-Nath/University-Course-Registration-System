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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit Test for RegistrationService
 * Tests course registration business logic with mocked dependencies
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
    private Registration registration;

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

        registration = new Registration();
        registration.setId(1L);
        registration.setStudent(student);
        registration.setCourse(course);
    }

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

        verify(userService).requireRole(Role.STUDENT);
        verify(courseService).getCourseOrThrow(1L);
        verify(registrationRepository).existsByStudentIdAndCourseId(1L, 1L);
        verify(registrationRepository).save(any(Registration.class));
    }

    @Test
    void register_WhenAlreadyRegistered_ShouldThrowConflictException() {
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

    @Test
    void drop_WhenRegistrationExists_ShouldDeleteRegistration() {
        // Given
        when(userService.requireRole(Role.STUDENT)).thenReturn(student);
        when(registrationRepository.findByStudentIdAndCourseId(1L, 1L))
                .thenReturn(Optional.of(registration));

        // When
        registrationService.drop(1L);

        // Then
        verify(registrationRepository).delete(registration);
    }

    @Test
    void drop_WhenRegistrationDoesNotExist_ShouldThrowNotFoundException() {
        // Given
        when(userService.requireRole(Role.STUDENT)).thenReturn(student);
        when(registrationRepository.findByStudentIdAndCourseId(1L, 1L))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> registrationService.drop(1L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Registration not found");

        verify(registrationRepository, never()).delete(any(Registration.class));
    }

    @Test
    void getMyRegistrations_ShouldReturnStudentRegistrations() {
        // Given
        Registration reg2 = new Registration();
        reg2.setId(2L);
        reg2.setStudent(student);

        when(userService.requireRole(Role.STUDENT)).thenReturn(student);
        when(registrationRepository.findAllByStudentId(1L))
                .thenReturn(List.of(registration, reg2));

        // When
        List<Registration> results = registrationService.getMyRegistrations();

        // Then
        assertThat(results).hasSize(2);
        assertThat(results).contains(registration, reg2);
        verify(registrationRepository).findAllByStudentId(1L);
    }

    @Test
    void getRegistrationsForCourse_WhenTeacherOwnsCourse_ShouldReturnRegistrations() {
        // Given
        when(userService.requireRole(Role.TEACHER)).thenReturn(teacher);
        when(courseService.getCourseOrThrow(1L)).thenReturn(course);
        when(registrationRepository.findAllByCourseId(1L)).thenReturn(List.of(registration));

        // When
        List<Registration> results = registrationService.getRegistrationsForCourse(1L);

        // Then
        assertThat(results).hasSize(1);
        assertThat(results.get(0)).isEqualTo(registration);
        verify(registrationRepository).findAllByCourseId(1L);
    }

    @Test
    void getRegistrationsForCourse_WhenTeacherDoesNotOwnCourse_ShouldThrowForbiddenException() {
        // Given
        User anotherTeacher = new User();
        anotherTeacher.setId(3L);
        anotherTeacher.setRole(Role.TEACHER);

        when(userService.requireRole(Role.TEACHER)).thenReturn(anotherTeacher);
        when(courseService.getCourseOrThrow(1L)).thenReturn(course);

        // When & Then
        assertThatThrownBy(() -> registrationService.getRegistrationsForCourse(1L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Not your course");

        verify(registrationRepository, never()).findAllByCourseId(any());
    }
}
