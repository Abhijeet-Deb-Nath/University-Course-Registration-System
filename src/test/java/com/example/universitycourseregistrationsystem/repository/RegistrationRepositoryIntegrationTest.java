package com.example.universitycourseregistrationsystem.repository;

import com.example.universitycourseregistrationsystem.domain.Course;
import com.example.universitycourseregistrationsystem.domain.Registration;
import com.example.universitycourseregistrationsystem.domain.Role;
import com.example.universitycourseregistrationsystem.domain.User;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration Test for RegistrationRepository
 *
 * Demonstrates repository integration testing with JPA and in-memory database.
 * Tests custom query methods and entity persistence.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class RegistrationRepositoryIntegrationTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private RegistrationRepository registrationRepository;

    private User student;
    private User teacher;
    private Course course;

    @BeforeEach
    void setUp() {
        // Create test teacher
        teacher = new User();
        teacher.setUsername("teacher1");
        teacher.setPasswordHash("hashedPassword");
        teacher.setRole(Role.TEACHER);
        entityManager.persist(teacher);

        // Create test student
        student = new User();
        student.setUsername("student1");
        student.setPasswordHash("hashedPassword");
        student.setRole(Role.STUDENT);
        entityManager.persist(student);

        // Create test course
        course = new Course();
        course.setCourseNo("CS101");
        course.setCourseName("Introduction to CS");
        course.setTeacher(teacher);
        entityManager.persist(course);

        entityManager.flush();
    }

    /**
     * Tests the existsByStudentIdAndCourseId method when registration exists.
     * Verifies the custom repository method returns true for existing records.
     */
    @Test
    void existsByStudentIdAndCourseId_WhenExists_ShouldReturnTrue() {
        // Given
        Registration registration = new Registration();
        registration.setStudent(student);
        registration.setCourse(course);
        entityManager.persist(registration);
        entityManager.flush();

        // When
        boolean exists = registrationRepository.existsByStudentIdAndCourseId(
                student.getId(),
                course.getId()
        );

        // Then
        assertThat(exists).isTrue();
    }

    /**
     * Tests the existsByStudentIdAndCourseId method when registration doesn't exist.
     * Verifies the custom repository method returns false for non-existing records.
     */
    @Test
    void existsByStudentIdAndCourseId_WhenNotExists_ShouldReturnFalse() {
        // When - Check for non-existent registration
        boolean exists = registrationRepository.existsByStudentIdAndCourseId(
                student.getId(),
                course.getId()
        );

        // Then
        assertThat(exists).isFalse();
    }

    /**
     * Tests findByStudentIdAndCourseId - verifies the query method
     * correctly finds a registration by student and course.
     */
    @Test
    void findByStudentIdAndCourseId_ShouldReturnRegistration() {
        // Given
        Registration registration = new Registration();
        registration.setStudent(student);
        registration.setCourse(course);
        entityManager.persist(registration);
        entityManager.flush();

        // When
        Optional<Registration> found = registrationRepository.findByStudentIdAndCourseId(
                student.getId(),
                course.getId()
        );

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getStudent().getUsername()).isEqualTo("student1");
        assertThat(found.get().getCourse().getCourseNo()).isEqualTo("CS101");
    }
}
