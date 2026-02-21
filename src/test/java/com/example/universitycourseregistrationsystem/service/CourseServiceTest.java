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
 * Tests course management business logic with mocked dependencies
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

    @Test
    void getAllCourses_ShouldReturnAllCourses() {
        // Given
        Course course2 = new Course();
        course2.setId(2L);
        course2.setCourseNo("CS102");
        course2.setCourseName("Data Structures");

        when(courseRepository.findAll()).thenReturn(Arrays.asList(course, course2));

        // When
        List<Course> results = courseService.getAllCourses();

        // Then
        assertThat(results).hasSize(2);
        assertThat(results).contains(course, course2);
        verify(courseRepository).findAll();
    }

    @Test
    void getTeacherCourses_ShouldReturnCoursesForTeacher() {
        // Given
        when(courseRepository.findAllByTeacherId(1L)).thenReturn(List.of(course));

        // When
        List<Course> results = courseService.getTeacherCourses(1L);

        // Then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getCourseNo()).isEqualTo("CS101");
        verify(courseRepository).findAllByTeacherId(1L);
    }

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
        assertThat(result.getCourseName()).isEqualTo("Algorithms");
        assertThat(result.getTeacher()).isEqualTo(teacher);

        verify(userService).requireRole(Role.TEACHER);
        verify(courseRepository).findByCourseNo("CS103");
        verify(courseRepository).save(any(Course.class));
    }

    @Test
    void createCourse_WhenCourseNoAlreadyExists_ShouldThrowConflictException() {
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

    @Test
    void updateCourse_WhenTeacherOwnsIt_ShouldUpdateCourse() {
        // Given
        CourseRequest request = new CourseRequest("CS101", "Updated Course Name");
        when(userService.requireRole(Role.TEACHER)).thenReturn(teacher);
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(courseRepository.save(any(Course.class))).thenReturn(course);

        // When
        Course result = courseService.updateCourse(1L, request);

        // Then
        assertThat(result.getCourseName()).isEqualTo("Updated Course Name");
        verify(courseRepository).save(course);
    }

    @Test
    void updateCourse_WhenTeacherDoesNotOwnIt_ShouldThrowForbiddenException() {
        // Given
        User anotherTeacher = new User();
        anotherTeacher.setId(2L);
        anotherTeacher.setRole(Role.TEACHER);

        CourseRequest request = new CourseRequest("CS101", "Updated Course Name");
        when(userService.requireRole(Role.TEACHER)).thenReturn(anotherTeacher);
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));

        // When & Then
        assertThatThrownBy(() -> courseService.updateCourse(1L, request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Not your course");

        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void deleteCourse_WhenTeacherOwnsIt_ShouldDeleteCourse() {
        // Given
        when(userService.requireRole(Role.TEACHER)).thenReturn(teacher);
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));

        // When
        courseService.deleteCourse(1L);

        // Then
        verify(courseRepository).delete(course);
    }

    @Test
    void deleteCourse_WhenTeacherDoesNotOwnIt_ShouldThrowForbiddenException() {
        // Given
        User anotherTeacher = new User();
        anotherTeacher.setId(2L);
        anotherTeacher.setRole(Role.TEACHER);

        when(userService.requireRole(Role.TEACHER)).thenReturn(anotherTeacher);
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));

        // When & Then
        assertThatThrownBy(() -> courseService.deleteCourse(1L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Not your course");

        verify(courseRepository, never()).delete(any(Course.class));
    }

    @Test
    void getCourseOrThrow_WhenCourseExists_ShouldReturnCourse() {
        // Given
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));

        // When
        Course result = courseService.getCourseOrThrow(1L);

        // Then
        assertThat(result).isEqualTo(course);
        verify(courseRepository).findById(1L);
    }

    @Test
    void getCourseOrThrow_WhenCourseDoesNotExist_ShouldThrowNotFoundException() {
        // Given
        when(courseRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> courseService.getCourseOrThrow(999L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Course not found");
    }
}
