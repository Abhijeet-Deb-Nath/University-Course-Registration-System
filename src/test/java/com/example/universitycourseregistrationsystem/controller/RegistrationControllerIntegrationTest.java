package com.example.universitycourseregistrationsystem.controller;

import com.example.universitycourseregistrationsystem.domain.Course;
import com.example.universitycourseregistrationsystem.domain.Registration;
import com.example.universitycourseregistrationsystem.domain.Role;
import com.example.universitycourseregistrationsystem.domain.User;
import com.example.universitycourseregistrationsystem.repository.CourseRepository;
import com.example.universitycourseregistrationsystem.repository.RegistrationRepository;
import com.example.universitycourseregistrationsystem.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration Test for Registration functionality
 *
 * Demonstrates integration testing by testing the repository layer
 * with real database operations and entity relationships.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class RegistrationControllerIntegrationTest {

    @Autowired
    private RegistrationRepository registrationRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User student;
    private User teacher;
    private Course course;

    @BeforeEach
    void setUp() {
        registrationRepository.deleteAll();
        courseRepository.deleteAll();
        userRepository.deleteAll();

        // Create test teacher
        teacher = new User();
        teacher.setUsername("teacher1");
        teacher.setPasswordHash(passwordEncoder.encode("password"));
        teacher.setRole(Role.TEACHER);
        teacher = userRepository.save(teacher);

        // Create test student
        student = new User();
        student.setUsername("student1");
        student.setPasswordHash(passwordEncoder.encode("password"));
        student.setRole(Role.STUDENT);
        student = userRepository.save(student);

        // Create test course
        course = new Course();
        course.setCourseNo("CS101");
        course.setCourseName("Introduction to Computer Science");
        course.setTeacher(teacher);
        course = courseRepository.save(course);
    }

    /**
     * Tests that a registration can be created and retrieved from the database.
     * Verifies the entity relationships are properly persisted.
     */
    @Test
    void registration_ShouldPersistToDatabase() {
        // Given - Create a registration
        Registration registration = new Registration();
        registration.setStudent(student);
        registration.setCourse(course);

        // When - Save to database
        Registration saved = registrationRepository.save(registration);

        // Then - Verify it was saved with proper relationships
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getStudent().getUsername()).isEqualTo("student1");
        assertThat(saved.getCourse().getCourseNo()).isEqualTo("CS101");
    }

    /**
     * Tests finding registrations by student - verifies the custom
     * repository query method works correctly.
     */
    @Test
    void findByStudent_ShouldReturnStudentRegistrations() {
        // Given - Create registrations for student
        Registration registration = new Registration();
        registration.setStudent(student);
        registration.setCourse(course);
        registrationRepository.save(registration);

        // When - Find by student
        List<Registration> registrations = registrationRepository.findAllByStudentId(student.getId());

        // Then - Verify registrations found
        assertThat(registrations).hasSize(1);
        assertThat(registrations.get(0).getCourse().getCourseNo()).isEqualTo("CS101");
    }
}
