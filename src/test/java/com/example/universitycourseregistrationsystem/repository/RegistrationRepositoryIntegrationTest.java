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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration Test for RegistrationRepository
 * Tests JPA repository methods with in-memory database
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
    private Course course1;
    private Course course2;

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

        // Create test courses
        course1 = new Course();
        course1.setCourseNo("CS101");
        course1.setCourseName("Introduction to CS");
        course1.setTeacher(teacher);
        entityManager.persist(course1);

        course2 = new Course();
        course2.setCourseNo("CS102");
        course2.setCourseName("Data Structures");
        course2.setTeacher(teacher);
        entityManager.persist(course2);

        entityManager.flush();
    }

    @Test
    void existsByStudentIdAndCourseId_WhenRegistrationExists_ShouldReturnTrue() {
        // Given
        Registration registration = new Registration();
        registration.setStudent(student);
        registration.setCourse(course1);
        entityManager.persist(registration);
        entityManager.flush();

        // When
        boolean exists = registrationRepository.existsByStudentIdAndCourseId(
                student.getId(),
                course1.getId()
        );

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void existsByStudentIdAndCourseId_WhenRegistrationDoesNotExist_ShouldReturnFalse() {
        // When
        boolean exists = registrationRepository.existsByStudentIdAndCourseId(
                student.getId(),
                course1.getId()
        );

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    void findByStudentIdAndCourseId_WhenRegistrationExists_ShouldReturnRegistration() {
        // Given
        Registration registration = new Registration();
        registration.setStudent(student);
        registration.setCourse(course1);
        registration = entityManager.persist(registration);
        entityManager.flush();

        // When
        Optional<Registration> found = registrationRepository.findByStudentIdAndCourseId(
                student.getId(),
                course1.getId()
        );

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getStudent().getUsername()).isEqualTo("student1");
        assertThat(found.get().getCourse().getCourseNo()).isEqualTo("CS101");
    }

    @Test
    void findByStudentIdAndCourseId_WhenRegistrationDoesNotExist_ShouldReturnEmpty() {
        // When
        Optional<Registration> found = registrationRepository.findByStudentIdAndCourseId(
                student.getId(),
                course1.getId()
        );

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    void findAllByStudentId_ShouldReturnAllRegistrationsForStudent() {
        // Given - Student registers for multiple courses
        Registration reg1 = new Registration();
        reg1.setStudent(student);
        reg1.setCourse(course1);
        entityManager.persist(reg1);

        Registration reg2 = new Registration();
        reg2.setStudent(student);
        reg2.setCourse(course2);
        entityManager.persist(reg2);

        // Create another student with registration (should not be returned)
        User anotherStudent = new User();
        anotherStudent.setUsername("student2");
        anotherStudent.setPasswordHash("hashedPassword");
        anotherStudent.setRole(Role.STUDENT);
        anotherStudent = entityManager.persist(anotherStudent);

        Registration reg3 = new Registration();
        reg3.setStudent(anotherStudent);
        reg3.setCourse(course1);
        entityManager.persist(reg3);

        entityManager.flush();

        // When
        List<Registration> registrations = registrationRepository.findAllByStudentId(student.getId());

        // Then
        assertThat(registrations).hasSize(2);
        assertThat(registrations).allMatch(r -> r.getStudent().getId().equals(student.getId()));
    }

    @Test
    void findAllByCourseId_ShouldReturnAllRegistrationsForCourse() {
        // Given - Multiple students register for same course
        User student2 = new User();
        student2.setUsername("student2");
        student2.setPasswordHash("hashedPassword");
        student2.setRole(Role.STUDENT);
        student2 = entityManager.persist(student2);

        Registration reg1 = new Registration();
        reg1.setStudent(student);
        reg1.setCourse(course1);
        entityManager.persist(reg1);

        Registration reg2 = new Registration();
        reg2.setStudent(student2);
        reg2.setCourse(course1);
        entityManager.persist(reg2);

        // Registration for different course (should not be returned)
        Registration reg3 = new Registration();
        reg3.setStudent(student);
        reg3.setCourse(course2);
        entityManager.persist(reg3);

        entityManager.flush();

        // When
        List<Registration> registrations = registrationRepository.findAllByCourseId(course1.getId());

        // Then
        assertThat(registrations).hasSize(2);
        assertThat(registrations).allMatch(r -> r.getCourse().getId().equals(course1.getId()));
    }

    @Test
    void deleteRegistration_ShouldRemoveFromDatabase() {
        // Given
        Registration registration = new Registration();
        registration.setStudent(student);
        registration.setCourse(course1);
        registration = entityManager.persist(registration);
        entityManager.flush();

        Long registrationId = registration.getId();

        // When
        registrationRepository.delete(registration);
        entityManager.flush();

        // Then
        Registration found = entityManager.find(Registration.class, registrationId);
        assertThat(found).isNull();
    }

    @Test
    void cascadeDelete_WhenCourseDeleted_ShouldDeleteRegistrations() {
        // Given
        Registration registration = new Registration();
        registration.setStudent(student);
        registration.setCourse(course1);
        entityManager.persist(registration);
        entityManager.flush();

        // When - Delete course (cascade should delete registrations)
        entityManager.remove(course1);
        entityManager.flush();
        entityManager.clear();

        // Then
        List<Registration> registrations = registrationRepository.findAllByCourseId(course1.getId());
        assertThat(registrations).isEmpty();
    }
}
