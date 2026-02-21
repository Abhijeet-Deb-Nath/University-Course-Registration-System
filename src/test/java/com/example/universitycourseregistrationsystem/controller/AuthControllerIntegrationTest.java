package com.example.universitycourseregistrationsystem.controller;

import com.example.universitycourseregistrationsystem.domain.Role;
import com.example.universitycourseregistrationsystem.dto.RegisterRequest;
import com.example.universitycourseregistrationsystem.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration Test for Authentication Controller
 *
 * Demonstrates integration testing by testing the full HTTP request/response cycle
 * with real Spring context and database operations.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    /**
     * Tests successful user registration - verifies the full flow from
     * HTTP request through controller, service, and repository layers.
     */
    @Test
    void register_WithValidData_ShouldCreateUser() throws Exception {
        RegisterRequest request = new RegisterRequest("newstudent", "password123", Role.STUDENT);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("newstudent"))
                .andExpect(jsonPath("$.role").value("STUDENT"))
                .andExpect(jsonPath("$.id").value(notNullValue()));
    }

    /**
     * Tests duplicate username handling - verifies proper error response
     * when attempting to register with an existing username.
     */
    @Test
    void register_WithDuplicateUsername_ShouldReturnConflict() throws Exception {
        // First registration
        RegisterRequest firstRequest = new RegisterRequest("duplicate", "password123", Role.STUDENT);
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(firstRequest)))
                .andExpect(status().isCreated());

        // Second registration with same username should fail
        RegisterRequest secondRequest = new RegisterRequest("duplicate", "password456", Role.TEACHER);
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(secondRequest)))
                .andExpect(status().isConflict());
    }
}
