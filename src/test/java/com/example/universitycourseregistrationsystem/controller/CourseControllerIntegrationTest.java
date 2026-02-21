package com.example.universitycourseregistrationsystem.controller;

import com.example.universitycourseregistrationsystem.domain.Course;
import com.example.universitycourseregistrationsystem.domain.Role;
import com.example.universitycourseregistrationsystem.domain.User;
import com.example.universitycourseregistrationsystem.repository.CourseRepository;
import com.example.universitycourseregistrationsystem.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration Test for Course Controller
 *
 * Demonstrates integration testing with real database and Spring context.
 * Tests the complete request/response flow including repository operations.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CourseControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User teacher;

    @BeforeEach
    void setUp() {
        courseRepository.deleteAll();
        userRepository.deleteAll();

        // Create test teacher
        teacher = new User();
        teacher.setUsername("teacher1");
        teacher.setPasswordHash(passwordEncoder.encode("password"));
        teacher.setRole(Role.TEACHER);
        teacher = userRepository.save(teacher);
    }

    /**
     * Tests listing all courses - verifies that courses are properly
     * retrieved from the database and returned as JSON.
     */
    @Test
    void listAllCourses_ShouldReturnAllCourses() throws Exception {
        // Given - Create test courses
        Course course1 = new Course();
        course1.setCourseNo("CS101");
        course1.setCourseName("Intro to CS");
        course1.setTeacher(teacher);
        courseRepository.save(course1);

        Course course2 = new Course();
        course2.setCourseNo("CS102");
        course2.setCourseName("Data Structures");
        course2.setTeacher(teacher);
        courseRepository.save(course2);

        // When & Then
        mockMvc.perform(get("/api/courses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].courseNo").value("CS101"))
                .andExpect(jsonPath("$[1].courseNo").value("CS102"));
    }

    /**
     * Tests empty course list - verifies correct behavior when no courses exist.
     */
    @Test
    void listAllCourses_WhenNoCourses_ShouldReturnEmptyList() throws Exception {
        mockMvc.perform(get("/api/courses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}
