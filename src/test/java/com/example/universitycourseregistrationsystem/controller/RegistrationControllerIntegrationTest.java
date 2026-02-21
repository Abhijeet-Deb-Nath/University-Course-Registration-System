package com.example.universitycourseregistrationsystem.controller;

import com.example.universitycourseregistrationsystem.domain.Course;
import com.example.universitycourseregistrationsystem.domain.Registration;
import com.example.universitycourseregistrationsystem.domain.Role;
import com.example.universitycourseregistrationsystem.domain.User;
import com.example.universitycourseregistrationsystem.dto.RegistrationRequest;
import com.example.universitycourseregistrationsystem.repository.CourseRepository;
import com.example.universitycourseregistrationsystem.repository.RegistrationRepository;
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
 * Integration Test for Course Registration
 * Tests the complete registration workflow with real database transactions
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Transactional
class RegistrationControllerIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RegistrationRepository registrationRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String studentToken;
    private String teacherToken;
    private User student;
    private User teacher;
    private Course course;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        registrationRepository.deleteAll();
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

        // Create test course
        course = new Course();
        course.setCourseNo("CS101");
        course.setCourseName("Introduction to Computer Science");
        course.setTeacher(teacher);
        course = courseRepository.save(course);
    }

    @Test
    void registerForCourse_AsStudent_ShouldCreateRegistration() throws Exception {
        // Given
        RegistrationRequest request = new RegistrationRequest(course.getId());

        // When & Then
        mockMvc.perform(post("/api/registrations")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.courseId").value(course.getId()))
                .andExpect(jsonPath("$.courseNo").value("CS101"))
                .andExpect(jsonPath("$.studentUsername").value("student1"))
                .andExpect(jsonPath("$.id").value(notNullValue()));
    }

    @Test
    void registerForCourse_AsTeacher_ShouldReturnForbidden() throws Exception {
        // Given
        RegistrationRequest request = new RegistrationRequest(course.getId());

        // When & Then
        mockMvc.perform(post("/api/registrations")
                        .header("Authorization", "Bearer " + teacherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void registerForCourse_WithoutAuth_ShouldReturnUnauthorized() throws Exception {
        // Given
        RegistrationRequest request = new RegistrationRequest(course.getId());

        // When & Then
        mockMvc.perform(post("/api/registrations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void registerForCourse_WhenAlreadyRegistered_ShouldReturnConflict() throws Exception {
        // Given - Register first time
        Registration existingReg = new Registration();
        existingReg.setStudent(student);
        existingReg.setCourse(course);
        registrationRepository.save(existingReg);

        RegistrationRequest request = new RegistrationRequest(course.getId());

        // When & Then - Try to register again
        mockMvc.perform(post("/api/registrations")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void registerForCourse_WithNonExistentCourse_ShouldReturnNotFound() throws Exception {
        // Given
        RegistrationRequest request = new RegistrationRequest(9999L);

        // When & Then
        mockMvc.perform(post("/api/registrations")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void dropCourse_WhenRegistered_ShouldDeleteRegistration() throws Exception {
        // Given - Register first
        Registration registration = new Registration();
        registration.setStudent(student);
        registration.setCourse(course);
        registrationRepository.save(registration);

        RegistrationRequest request = new RegistrationRequest(course.getId());

        // When & Then
        mockMvc.perform(delete("/api/registrations")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        // Verify registration is deleted
        mockMvc.perform(get("/api/registrations/mine")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void dropCourse_WhenNotRegistered_ShouldReturnNotFound() throws Exception {
        // Given
        RegistrationRequest request = new RegistrationRequest(course.getId());

        // When & Then
        mockMvc.perform(delete("/api/registrations")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void getMyRegistrations_ShouldReturnStudentRegistrations() throws Exception {
        // Given - Create multiple registrations
        Course course2 = new Course();
        course2.setCourseNo("CS102");
        course2.setCourseName("Data Structures");
        course2.setTeacher(teacher);
        course2 = courseRepository.save(course2);

        Registration reg1 = new Registration();
        reg1.setStudent(student);
        reg1.setCourse(course);
        registrationRepository.save(reg1);

        Registration reg2 = new Registration();
        reg2.setStudent(student);
        reg2.setCourse(course2);
        registrationRepository.save(reg2);

        // When & Then
        mockMvc.perform(get("/api/registrations/mine")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].studentUsername").value("student1"))
                .andExpect(jsonPath("$[1].studentUsername").value("student1"));
    }

    @Test
    void getMyRegistrations_WhenNoRegistrations_ShouldReturnEmptyList() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/registrations/mine")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void getCourseStudents_AsTeacher_ShouldReturnRegistrations() throws Exception {
        // Given - Create registrations from multiple students
        User student2 = new User();
        student2.setUsername("student2");
        student2.setPasswordHash(passwordEncoder.encode("password"));
        student2.setRole(Role.STUDENT);
        student2 = userRepository.save(student2);

        Registration reg1 = new Registration();
        reg1.setStudent(student);
        reg1.setCourse(course);
        registrationRepository.save(reg1);

        Registration reg2 = new Registration();
        reg2.setStudent(student2);
        reg2.setCourse(course);
        registrationRepository.save(reg2);

        // When & Then
        mockMvc.perform(get("/api/courses/" + course.getId() + "/students")
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].courseNo").value("CS101"))
                .andExpect(jsonPath("$[1].courseNo").value("CS101"));
    }

    @Test
    void getCourseStudents_AsNonOwnerTeacher_ShouldReturnForbidden() throws Exception {
        // Given - Create another teacher
        User anotherTeacher = new User();
        anotherTeacher.setUsername("teacher2");
        anotherTeacher.setPasswordHash(passwordEncoder.encode("password"));
        anotherTeacher.setRole(Role.TEACHER);
        anotherTeacher = userRepository.save(anotherTeacher);
        String anotherTeacherToken = jwtService.generateToken(anotherTeacher.getUsername(), Map.of("role", "TEACHER"));

        // When & Then
        mockMvc.perform(get("/api/courses/" + course.getId() + "/students")
                        .header("Authorization", "Bearer " + anotherTeacherToken))
                .andExpect(status().isForbidden());
    }
}
