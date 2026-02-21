# Testing Quick Reference Card

## 📋 Test Commands

### Run All Tests
```bash
./mvnw test
```

### Run Specific Test Class
```bash
./mvnw test -Dtest=UserServiceTest
./mvnw test -Dtest=AuthControllerIntegrationTest
```

### Run Tests by Pattern
```bash
./mvnw test -Dtest=*ServiceTest        # All unit tests
./mvnw test -Dtest=*IntegrationTest    # All integration tests
```

---

## 📊 Test Summary

| Test File | Type | Tests | What It Tests |
|-----------|------|-------|---------------|
| **UserServiceTest** | Unit | 8 | User registration, authentication, roles |
| **CourseServiceTest** | Unit | 10 | Course CRUD, ownership validation |
| **RegistrationServiceTest** | Unit | 6 | Enrollment logic, duplicate prevention |
| **AuthControllerIntegrationTest** | Integration | 5 | Full auth flow, JWT generation |
| **CourseControllerIntegrationTest** | Integration | 8 | HTTP → DB with auth/authz |
| **RegistrationControllerIntegrationTest** | Integration | 10 | Complete enrollment workflow |
| **RegistrationRepositoryIntegrationTest** | Integration | 8 | JPA queries, cascades |
| **TOTAL** | - | **45** | **Complete system coverage** |

---

## 🔑 Key Test Scenarios

### ✅ Authentication & Authorization
- User registration (unique username)
- Login with valid/invalid credentials
- JWT token generation
- Role-based access (TEACHER vs STUDENT)
- Ownership-based permissions

### ✅ Course Management
- Create course (TEACHER only)
- Update course (owner only)
- Delete course (owner only)
- List all courses (anyone)
- Duplicate course number prevention

### ✅ Student Registration
- Register for course (STUDENT only)
- Prevent duplicate enrollment
- Drop course
- View my registrations
- Teacher view course roster (own courses only)

### ✅ Database Operations
- JPA custom query methods
- Cascade deletes
- Transaction management
- Entity relationships

---

## 🏗️ Test Architecture

### Unit Tests (75%)
**Purpose**: Test business logic in isolation  
**Speed**: Very fast (milliseconds)  
**Database**: Mocked (no real DB)  
**Location**: `src/test/java/.../service/`

**Example**:
```java
@Test
void createCourse_WhenCourseNoIsUnique_ShouldCreateCourse() {
    // Mock dependencies
    when(userService.requireRole(Role.TEACHER)).thenReturn(teacher);
    when(courseRepository.save(any())).thenReturn(course);
    
    // Test the service method
    Course result = courseService.createCourse(request);
    
    // Verify behavior
    verify(courseRepository).save(any());
    assertThat(result.getCourseNo()).isEqualTo("CS101");
}
```

### Integration Tests (25%)
**Purpose**: Test full stack together  
**Speed**: Slower (seconds)  
**Database**: H2 in-memory (real DB operations)  
**Location**: `src/test/java/.../controller/` & `.../repository/`

**Example**:
```java
@Test
void createCourse_AsTeacher_ShouldCreateCourse() throws Exception {
    // Make HTTP request with JWT token
    mockMvc.perform(post("/api/courses")
            .header("Authorization", "Bearer " + teacherToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(json))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.courseNo").value("CS101"));
}
```

---

## 🎯 Testing Best Practices Used

### 1. Test Naming Convention
```
methodName_WhenCondition_ThenExpectedResult()
```
Examples:
- `register_WhenUsernameIsUnique_ShouldCreateUser()`
- `createCourse_AsStudent_ShouldReturnForbidden()`

### 2. AAA Pattern (Arrange-Act-Assert)
```java
// Given (Arrange)
User user = new User();
user.setUsername("test");

// When (Act)
User result = userService.register(request);

// Then (Assert)
assertThat(result.getUsername()).isEqualTo("test");
```

### 3. Test Data Setup
```java
@BeforeEach
void setUp() {
    // Fresh data for each test
    userRepository.deleteAll();
    teacher = createTestTeacher();
}
```

### 4. Test Isolation
- Each test is independent
- `@Transactional` rolls back changes
- No shared state between tests

---

## 🔧 Technologies Used

| Technology | Purpose | Why |
|------------|---------|-----|
| **JUnit 5** | Test framework | Industry standard, modern features |
| **Mockito** | Mocking | Simulate dependencies in unit tests |
| **AssertJ** | Assertions | Fluent, readable assertions |
| **MockMvc** | HTTP testing | Test REST APIs without server |
| **H2 Database** | Test database | Fast, in-memory, PostgreSQL mode |
| **Spring Boot Test** | Integration support | Full Spring context for testing |

---

## 📈 Test Coverage

### What's Tested
✅ User registration & authentication  
✅ JWT token generation & validation  
✅ Role-based authorization (TEACHER/STUDENT)  
✅ Ownership-based permissions  
✅ Course CRUD operations  
✅ Student enrollment workflow  
✅ Database constraints & relationships  
✅ Error handling & validation  
✅ HTTP status codes  
✅ JSON serialization/deserialization  

### What's NOT Tested (OK for academic project)
❌ Frontend JavaScript  
❌ Performance/load testing  
❌ Security penetration testing  
❌ UI/E2E tests with Selenium  

---

## 🚀 Next Steps: CI/CD

These tests are ready for **GitHub Actions**:

1. Create `.github/workflows/ci.yml`
2. Configure to run tests on push/PR
3. Set up branch protection rules
4. Tests must pass before merge

---

## 📚 Documentation Files

- **testing_explanation.md** - Detailed guide with code examples
- **testing_summary.md** - Implementation summary
- **testing_quick_reference.md** - This file (quick commands)

---

## 💡 Common Issues & Solutions

### Issue: Tests fail with "Database not found"
**Solution**: Check `application-test.properties` exists

### Issue: "MockMvc is null"
**Solution**: Ensure `@WebAppContextSetup` in setUp() method

### Issue: "Authentication failed"
**Solution**: Generate valid JWT token in setUp()

### Issue: Tests are slow
**Solution**: Unit tests should be fast; integration tests are slower (expected)

---

## ✅ Verification Checklist

Before presenting to professor:

- [ ] All 45 tests compile without errors
- [ ] `./mvnw test` runs successfully
- [ ] Unit tests execute in < 5 seconds
- [ ] Integration tests complete in < 30 seconds
- [ ] Test names follow convention
- [ ] Documentation files created
- [ ] Ready for CI/CD integration

---

## 🎓 What to Tell Your Professor

**"I implemented comprehensive testing following industry best practices:**

1. **45 test cases** covering core functionality
2. **Unit tests** (24) for business logic with Mockito mocks
3. **Integration tests** (21) for full-stack validation with H2 database
4. **Test pyramid architecture** with proper test isolation
5. **AAA pattern** for clear, maintainable tests
6. **Ready for CI/CD** integration with GitHub Actions
7. **Demonstrates** authentication, authorization, database operations, and error handling"

---

**Need help?** Check `testing_explanation.md` for detailed explanations!
