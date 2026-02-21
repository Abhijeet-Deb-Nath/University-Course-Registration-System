package com.example.universitycourseregistrationsystem.service;

import com.example.universitycourseregistrationsystem.domain.Course;
import com.example.universitycourseregistrationsystem.domain.Role;
import com.example.universitycourseregistrationsystem.domain.User;
import com.example.universitycourseregistrationsystem.dto.CourseRequest;
import com.example.universitycourseregistrationsystem.repository.CourseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit Test for CourseService
 *
 * Demonstrates unit testing with mocked dependencies using Mockito.
 * Tests business logic in isolation from database and other services.
 */
@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private CourseService courseService;

    private User teacher;
    private Course course;

    @BeforeEach
    void setUp() {
        teacher = new User();
        teacher.setId(1L);
        teacher.setUsername("teacher1");
        teacher.setRole(Role.TEACHER);

        course = new Course();
        course.setId(1L);
        course.setCourseNo("CS101");
        course.setCourseName("Introduction to Computer Science");
        course.setTeacher(teacher);
    }

    /**
     * Tests getAllCourses - verifies the service correctly retrieves
     * all courses from the repository.
     */
    @Test
    void getAllCourses_ShouldReturnAllCourses() {
        // Given
        Course course2 = new Course();
        course2.setId(2L);
        course2.setCourseNo("CS102");
        course2.setCourseName("Data Structures");
        course2.setTeacher(teacher);

        when(courseRepository.findAll()).thenReturn(Arrays.asList(course, course2));

        // When
        List<Course> results = courseService.getAllCourses();

        // Then
        assertThat(results).hasSize(2);
        verify(courseRepository).findAll();
    }

    /**
     * Tests createCourse with unique course number - verifies the service
     * creates a course when the course number doesn't already exist.
     */
    @Test
    void createCourse_WhenCourseNoIsUnique_ShouldCreateCourse() {
        // Given
        CourseRequest request = new CourseRequest("CS103", "Algorithms");
        when(userService.requireRole(Role.TEACHER)).thenReturn(teacher);
        when(courseRepository.findByCourseNo("CS103")).thenReturn(Optional.empty());
        when(courseRepository.save(any(Course.class))).thenAnswer(invocation -> {
            Course savedCourse = invocation.getArgument(0);
            savedCourse.setId(3L);
            return savedCourse;
        });

        // When
        Course result = courseService.createCourse(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getCourseNo()).isEqualTo("CS103");
        assertThat(result.getTeacher()).isEqualTo(teacher);
        verify(courseRepository).save(any(Course.class));
    }

    /**
     * Tests createCourse with duplicate course number - verifies the service
     * throws an exception when course number already exists.
     */
    @Test
    void createCourse_WhenCourseNoExists_ShouldThrowConflict() {
        // Given
        CourseRequest request = new CourseRequest("CS101", "Duplicate Course");
        when(userService.requireRole(Role.TEACHER)).thenReturn(teacher);
        when(courseRepository.findByCourseNo("CS101")).thenReturn(Optional.of(course));

        // When & Then
        assertThatThrownBy(() -> courseService.createCourse(request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Course number already exists");

        verify(courseRepository, never()).save(any(Course.class));
    }
}
