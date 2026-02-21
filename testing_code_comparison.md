# Code Comparison: Mock vs Real Database

## 🔍 **Seeing the Actual Difference in Code**

---

## Example 1: Testing User Registration

### **Unit Test - Mocked Database**

```java
package com.example.universitycourseregistrationsystem.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)  // ← Using Mockito, NOT Spring
class UserServiceTest {
    
    // ❌ FAKE - Mockito creates a fake repository
    @Mock
    private UserRepository userRepository;
    
    // ❌ FAKE - Mockito creates a fake encoder
    @Mock
    private PasswordEncoder passwordEncoder;
    
    // ✅ REAL - but uses the fake dependencies above
    @InjectMocks
    private UserService userService;
    
    @Test
    void register_WhenUsernameIsUnique_ShouldCreateUser() {
        // ARRANGE: Tell mocks what to return
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });
        
        // ACT: Call the real service method
        RegisterRequest request = new RegisterRequest("newuser", "password123", Role.STUDENT);
        User result = userService.register(request);
        
        // ASSERT: Check behavior
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("newuser");
        assertThat(result.getPasswordHash()).isEqualTo("encodedPassword");
        
        // VERIFY: Did service call the mocks correctly?
        verify(userRepository).existsByUsername("newuser");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
    }
}
```

**Key Points:**
- ❌ No `@SpringBootTest` - Not loading Spring context
- ❌ `@Mock` - Creating fake objects
- ❌ `when(...).thenReturn(...)` - Telling mocks what to return
- ❌ `verify(...)` - Checking if methods were called
- ❌ **No real database** - Everything is simulated
- ✅ **Super fast** - No database startup

---

### **Integration Test - Real Database**

```java
package com.example.universitycourseregistrationsystem.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)  // ← Full Spring context
@ActiveProfiles("test")  // ← Use test configuration (H2 database)
@Transactional  // ← Each test runs in transaction (auto-rollback)
class AuthControllerIntegrationTest {
    
    private MockMvc mockMvc;
    
    @Autowired  // ✅ REAL Spring-managed WebApplicationContext
    private WebApplicationContext webApplicationContext;
    
    @Autowired  // ✅ REAL ObjectMapper from Spring
    private ObjectMapper objectMapper;
    
    @Autowired  // ✅ REAL JPA repository with H2 database
    private UserRepository userRepository;
    
    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        userRepository.deleteAll();  // ✅ REAL database DELETE
    }
    
    @Test
    void register_WithValidData_ShouldCreateUser() throws Exception {
        // ARRANGE: Prepare request
        RegisterRequest request = new RegisterRequest("newstudent", "password123", Role.STUDENT);
        
        // ACT: Make REAL HTTP request (simulated, but goes through full Spring stack)
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("newstudent"))
                .andExpect(jsonPath("$.role").value("STUDENT"))
                .andExpect(jsonPath("$.id").value(notNullValue()));
        
        // ASSERT: Query REAL database to verify
        List<User> users = userRepository.findAll();  // ✅ REAL SQL SELECT
        assertThat(users).hasSize(1);
        assertThat(users.get(0).getUsername()).isEqualTo("newstudent");
        
        // Verify password was REALLY hashed
        String hash = users.get(0).getPasswordHash();
        assertThat(hash).isNotEqualTo("password123");  // Not plain text!
        assertThat(hash).startsWith("$2a$");  // BCrypt format
    }
}
```

**Key Points:**
- ✅ `@SpringBootTest` - Loads entire Spring context
- ✅ `@Autowired` - Real Spring beans
- ✅ `userRepository.findAll()` - **REAL SQL query to H2**
- ✅ `userRepository.deleteAll()` - **REAL database DELETE**
- ✅ **Real HTTP flow** - Request → Controller → Service → Repository → H2
- ✅ **Real password hashing** - BCrypt actually runs
- ⏱️ **Slower** - But tests the entire system

---

## Example 2: Testing Course Creation

### **Unit Test - Mocked**

```java
@ExtendWith(MockitoExtension.class)
class CourseServiceTest {
    
    @Mock  // ❌ FAKE
    private CourseRepository courseRepository;
    
    @Mock  // ❌ FAKE
    private UserService userService;
    
    @InjectMocks  // ✅ REAL (with fake dependencies)
    private CourseService courseService;
    
    @Test
    void createCourse_WhenCourseNoIsUnique_ShouldCreateCourse() {
        // Setup fake teacher
        User teacher = new User();
        teacher.setId(1L);
        teacher.setRole(Role.TEACHER);
        
        // Tell mocks what to return
        when(userService.requireRole(Role.TEACHER)).thenReturn(teacher);
        when(courseRepository.findByCourseNo("CS101")).thenReturn(Optional.empty());
        when(courseRepository.save(any(Course.class))).thenAnswer(invocation -> {
            Course savedCourse = invocation.getArgument(0);
            savedCourse.setId(1L);
            return savedCourse;
        });
        
        // Test
        CourseRequest request = new CourseRequest("CS101", "Intro to CS");
        Course result = courseService.createCourse(request);
        
        // Verify
        assertThat(result.getCourseNo()).isEqualTo("CS101");
        verify(courseRepository).findByCourseNo("CS101");  // Was duplicate check called?
        verify(courseRepository).save(any(Course.class));   // Was save called?
    }
}
```

**What we're testing:**
- ✅ Does service check for duplicates before saving?
- ✅ Does service set the teacher correctly?
- ✅ Does service call repository.save()?

**What we're NOT testing:**
- ❌ Does the course actually get saved to database?
- ❌ Does the unique constraint work in database?

---

### **Integration Test - Real Database**

```java
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Transactional
class CourseControllerIntegrationTest {
    
    private MockMvc mockMvc;
    
    @Autowired
    private WebApplicationContext webApplicationContext;
    
    @Autowired  // ✅ REAL repository with H2 connection
    private CourseRepository courseRepository;
    
    @Autowired  // ✅ REAL repository with H2 connection
    private UserRepository userRepository;
    
    @Autowired  // ✅ REAL JWT service
    private JwtService jwtService;
    
    private String teacherToken;
    private User teacher;
    
    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        
        // Create REAL teacher in H2 database
        teacher = new User();
        teacher.setUsername("teacher1");
        teacher.setPasswordHash(passwordEncoder.encode("password"));
        teacher.setRole(Role.TEACHER);
        teacher = userRepository.save(teacher);  // ✅ REAL INSERT into H2
        
        // Generate REAL JWT token
        teacherToken = jwtService.generateToken(teacher.getUsername(), Map.of("role", "TEACHER"));
    }
    
    @Test
    void createCourse_AsTeacher_ShouldCreateCourse() throws Exception {
        // ARRANGE
        CourseRequest request = new CourseRequest("CS201", "Data Structures");
        
        // ACT: Make REAL HTTP POST request
        mockMvc.perform(post("/api/courses")
                        .header("Authorization", "Bearer " + teacherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.courseNo").value("CS201"))
                .andExpect(jsonPath("$.courseName").value("Data Structures"));
        
        // ASSERT: Query REAL database
        List<Course> courses = courseRepository.findAll();  // ✅ REAL SELECT from H2
        assertThat(courses).hasSize(1);
        assertThat(courses.get(0).getCourseNo()).isEqualTo("CS201");
        assertThat(courses.get(0).getTeacher().getId()).isEqualTo(teacher.getId());
    }
    
    @Test
    void createCourse_WithDuplicateCourseNo_ShouldReturnConflict() throws Exception {
        // Create existing course in REAL database
        Course existing = new Course();
        existing.setCourseNo("CS101");
        existing.setCourseName("Existing Course");
        existing.setTeacher(teacher);
        courseRepository.save(existing);  // ✅ REAL INSERT
        
        // Try to create duplicate
        CourseRequest request = new CourseRequest("CS101", "Duplicate Course");
        
        mockMvc.perform(post("/api/courses")
                        .header("Authorization", "Bearer " + teacherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());  // Should fail!
        
        // Verify only 1 course in database (duplicate was rejected)
        assertThat(courseRepository.count()).isEqualTo(1);  // ✅ REAL COUNT query
    }
}
```

**What we're testing:**
- ✅ Full HTTP → Controller → Security → Service → Repository → H2 flow
- ✅ JWT authentication works
- ✅ Role authorization works (TEACHER can create)
- ✅ Course is ACTUALLY saved to H2 database
- ✅ Duplicate check works in REAL database
- ✅ Database returns correct error
- ✅ Transaction rolls back on error

---

## Key Differences Highlighted

| Aspect | Unit Test (Mock) | Integration Test (Real) |
|--------|------------------|------------------------|
| **Annotation** | `@ExtendWith(MockitoExtension.class)` | `@SpringBootTest` |
| **Dependencies** | `@Mock` (fake) | `@Autowired` (real) |
| **Service** | `@InjectMocks` | `@Autowired` |
| **Setup** | `when(...).thenReturn(...)` | Create real data with `save()` |
| **Test** | Call method directly | HTTP request via `MockMvc` |
| **Verification** | `verify(mock).method()` | Query database with `findAll()` |
| **Database** | ❌ None | ✅ H2 in-memory |
| **Spring Context** | ❌ Not loaded | ✅ Fully loaded |
| **Speed** | ⚡ Milliseconds | ⏱️ Seconds |
| **What's Tested** | Logic only | Full system |

---

## How to Tell Them Apart in Code

### **Unit Test Indicators:**
```java
❌ @ExtendWith(MockitoExtension.class)  // Not Spring
❌ @Mock                                 // Fake object
❌ @InjectMocks                          // Real object with fakes
❌ when(...).thenReturn(...)             // Setting up fake behavior
❌ verify(...)                           // Checking method calls
```

### **Integration Test Indicators:**
```java
✅ @SpringBootTest                       // Full Spring context
✅ @Autowired                            // Real Spring bean
✅ repository.save(...)                  // Real database operation
✅ repository.findAll()                  // Real SQL query
✅ mockMvc.perform(...)                  // Real HTTP simulation
✅ @Transactional                        // Real transaction
```

---

## Summary

### **Unit Test (Mock):**
- Tests **one class** in isolation
- Everything else is **fake**
- Verifies **logic** and **method calls**
- **Cannot test database** behavior

### **Integration Test (Real):**
- Tests **entire system** together
- Everything is **real**
- Verifies **actual persistence** and **SQL queries**
- **Required** to test database behavior

### **Both Are Essential:**
- Unit tests: Fast feedback on logic
- Integration tests: Confidence system works end-to-end

You **CANNOT** have a production-ready app with only unit tests!
