package com.example.universitycourseregistrationsystem.controller;

import com.example.universitycourseregistrationsystem.domain.Course;
import com.example.universitycourseregistrationsystem.domain.Role;
import com.example.universitycourseregistrationsystem.domain.User;
import com.example.universitycourseregistrationsystem.dto.CourseRequest;
import com.example.universitycourseregistrationsystem.repository.CourseRepository;
import com.example.universitycourseregistrationsystem.repository.UserRepository;
import com.example.universitycourseregistrationsystem.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration Test for Course Management
 * Tests the full stack including authentication, authorization, and database operations
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Transactional
class CourseControllerIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String teacherToken;
    private String studentToken;
    private User teacher;
    private User student;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        courseRepository.deleteAll();
        userRepository.deleteAll();

        // Create test teacher
        teacher = new User();
        teacher.setUsername("teacher1");
        teacher.setPasswordHash(passwordEncoder.encode("password"));
        teacher.setRole(Role.TEACHER);
        teacher = userRepository.save(teacher);
        teacherToken = jwtService.generateToken(teacher.getUsername(), Map.of("role", "TEACHER"));

        // Create test student
        student = new User();
        student.setUsername("student1");
        student.setPasswordHash(passwordEncoder.encode("password"));
        student.setRole(Role.STUDENT);
        student = userRepository.save(student);
        studentToken = jwtService.generateToken(student.getUsername(), Map.of("role", "STUDENT"));
    }

    @Test
    void listAllCourses_WithoutAuth_ShouldReturnCourses() throws Exception {
        // Given
        Course course = new Course();
        course.setCourseNo("CS101");
        course.setCourseName("Intro to CS");
        course.setTeacher(teacher);
        courseRepository.save(course);

        // When & Then
        mockMvc.perform(get("/api/courses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].courseNo").value("CS101"))
                .andExpect(jsonPath("$[0].courseName").value("Intro to CS"));
    }

    @Test
    void createCourse_AsTeacher_ShouldCreateCourse() throws Exception {
        // Given
        CourseRequest request = new CourseRequest("CS201", "Data Structures");

        // When & Then
        mockMvc.perform(post("/api/courses")
                        .header("Authorization", "Bearer " + teacherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.courseNo").value("CS201"))
                .andExpect(jsonPath("$.courseName").value("Data Structures"))
                .andExpect(jsonPath("$.teacherId").value(teacher.getId()))
                .andExpect(jsonPath("$.id").value(notNullValue()));
    }

    @Test
    void createCourse_AsStudent_ShouldReturnForbidden() throws Exception {
        // Given
        CourseRequest request = new CourseRequest("CS201", "Data Structures");

        // When & Then
        mockMvc.perform(post("/api/courses")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void createCourse_WithoutAuth_ShouldReturnUnauthorized() throws Exception {
        // Given
        CourseRequest request = new CourseRequest("CS201", "Data Structures");

        // When & Then
        mockMvc.perform(post("/api/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createCourse_WithDuplicateCourseNo_ShouldReturnConflict() throws Exception {
        // Given - Create first course
        Course existingCourse = new Course();
        existingCourse.setCourseNo("CS101");
        existingCourse.setCourseName("Existing Course");
        existingCourse.setTeacher(teacher);
        courseRepository.save(existingCourse);

        // When - Try to create course with same course number
        CourseRequest request = new CourseRequest("CS101", "Duplicate Course");

        // Then
        mockMvc.perform(post("/api/courses")
                        .header("Authorization", "Bearer " + teacherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void updateCourse_AsOwner_ShouldUpdateCourse() throws Exception {
        // Given
        Course course = new Course();
        course.setCourseNo("CS101");
        course.setCourseName("Original Name");
        course.setTeacher(teacher);
        course = courseRepository.save(course);

        CourseRequest updateRequest = new CourseRequest("CS101", "Updated Name");

        // When & Then
        mockMvc.perform(put("/api/courses/" + course.getId())
                        .header("Authorization", "Bearer " + teacherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.courseName").value("Updated Name"));
    }

    @Test
    void updateCourse_AsNonOwner_ShouldReturnForbidden() throws Exception {
        // Given - Create another teacher
        User anotherTeacher = new User();
        anotherTeacher.setUsername("teacher2");
        anotherTeacher.setPasswordHash(passwordEncoder.encode("password"));
        anotherTeacher.setRole(Role.TEACHER);
        anotherTeacher = userRepository.save(anotherTeacher);
        String anotherTeacherToken = jwtService.generateToken(anotherTeacher.getUsername(), Map.of("role", "TEACHER"));

        Course course = new Course();
        course.setCourseNo("CS101");
        course.setCourseName("Original Name");
        course.setTeacher(teacher);
        course = courseRepository.save(course);

        CourseRequest updateRequest = new CourseRequest("CS101", "Updated Name");

        // When & Then
        mockMvc.perform(put("/api/courses/" + course.getId())
                        .header("Authorization", "Bearer " + anotherTeacherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteCourse_AsOwner_ShouldDeleteCourse() throws Exception {
        // Given
        Course course = new Course();
        course.setCourseNo("CS101");
        course.setCourseName("To Be Deleted");
        course.setTeacher(teacher);
        course = courseRepository.save(course);

        // When & Then
        mockMvc.perform(delete("/api/courses/" + course.getId())
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isNoContent());

        // Verify course is deleted
        mockMvc.perform(get("/api/courses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void listMyCourses_AsTeacher_ShouldReturnOnlyMyCourses() throws Exception {
        // Given - Create courses for different teachers
        Course myCourse = new Course();
        myCourse.setCourseNo("CS101");
        myCourse.setCourseName("My Course");
        myCourse.setTeacher(teacher);
        courseRepository.save(myCourse);

        User anotherTeacher = new User();
        anotherTeacher.setUsername("teacher2");
        anotherTeacher.setPasswordHash(passwordEncoder.encode("password"));
        anotherTeacher.setRole(Role.TEACHER);
        anotherTeacher = userRepository.save(anotherTeacher);

        Course otherCourse = new Course();
        otherCourse.setCourseNo("CS102");
        otherCourse.setCourseName("Other Course");
        otherCourse.setTeacher(anotherTeacher);
        courseRepository.save(otherCourse);

        // When & Then
        mockMvc.perform(get("/api/courses/mine")
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].courseNo").value("CS101"));
    }
}
