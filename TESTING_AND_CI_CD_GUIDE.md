# Testing & CI/CD Implementation Guide

This document explains the testing strategy, CI/CD pipeline, and branch protection implementation in the University Course Registration System.

---

## Table of Contents
1. [Overview](#overview)
2. [Unit Testing](#unit-testing)
3. [Integration Testing](#integration-testing)
4. [CI/CD Pipeline](#cicd-pipeline)
5. [Branch Protection Rules](#branch-protection-rules)
6. [Complete Workflow](#complete-workflow)

---

## Overview

### What Problem Do We Solve?

| Problem | Solution | Benefit |
|---------|----------|---------|
| **Manual Testing is Slow** | Automated tests run in seconds | Save hours of manual QA |
| **Bugs Reach Production** | Tests catch bugs before merge | Higher code quality |
| **Breaking Changes** | CI fails if tests fail | Prevents bad code from merging |
| **Inconsistent Code Quality** | Branch protection enforces reviews | Team collaboration |

### Testing Pyramid

```
                    /\
                   /  \         Manual/E2E Tests
                  /────\        (Few, Slow, Expensive)
                 /      \
                / Integr-\      Integration Tests  
               /  ation   \     (Some, Medium)
              /────────────\
             /              \
            /  Unit Tests    \   Unit Tests
           /__________________\  (Many, Fast, Cheap)
```

**Our Implementation:**
- **Unit Tests**: 6 tests (UserService, CourseService, RegistrationService)
- **Integration Tests**: 8 tests (Controllers, Repositories)
- **Total**: 14 automated tests

---

## Unit Testing

### What is Unit Testing?

Unit tests verify **individual components in isolation**. Dependencies are replaced with "mocks" (fake objects) to test only the business logic.

### File Locations
```
src/test/java/.../service/
├── UserServiceTest.java          (2 tests)
├── CourseServiceTest.java        (2 tests)
└── RegistrationServiceTest.java  (2 tests)
```

### Key Concepts

#### 1. Mocking with Mockito

```java
@ExtendWith(MockitoExtension.class)  // Enable Mockito
class UserServiceTest {
    
    @Mock  // Create fake UserRepository
    private UserRepository userRepository;
    
    @Mock  // Create fake PasswordEncoder
    private PasswordEncoder passwordEncoder;
    
    @InjectMocks  // Inject mocks into UserService
    private UserService userService;
}
```

**Why Mock?**
- **Speed**: No real database, tests run in milliseconds
- **Isolation**: Test only the service logic
- **Control**: Predictable behavior

#### 2. Test Structure: Arrange-Act-Assert

```java
@Test
void register_WithUniqueUsername_ShouldCreateUser() {
    // ARRANGE - Setup test data and mock behavior
    RegisterRequest request = new RegisterRequest("john", "pass", Role.STUDENT);
    when(userRepository.existsByUsername("john")).thenReturn(false);
    when(passwordEncoder.encode("pass")).thenReturn("hashed");
    
    // ACT - Call the method under test
    User result = userService.register(request);
    
    // ASSERT - Verify the outcome
    assertThat(result.getUsername()).isEqualTo("john");
    verify(userRepository).save(any(User.class));
}
```

#### 3. How Mocking Works

```
WITHOUT MOCKING (Slow, Complex)
┌──────────────────────────────────────────┐
│ Test → Service → Repository → Database   │
│   ❌ Slow (network I/O)                  │
│   ❌ Needs database running              │
│   ❌ Data cleanup required               │
└──────────────────────────────────────────┘

WITH MOCKING (Fast, Simple)
┌──────────────────────────────────────────┐
│ Test → Service → Mock Repository (fake)  │
│   ✅ Fast (in-memory)                    │
│   ✅ No database needed                  │
│   ✅ Predictable results                 │
└──────────────────────────────────────────┘
```

### Example: CourseServiceTest

```java
@Test
void createCourse_ShouldSaveAndReturnCourse() {
    // Arrange
    CourseRequest request = new CourseRequest("CS101", "Intro to CS");
    when(userService.requireRole(Role.TEACHER)).thenReturn(teacher);
    when(courseRepository.findByCourseNo("CS101")).thenReturn(Optional.empty());
    when(courseRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    
    // Act
    Course result = courseService.createCourse(request);
    
    // Assert
    assertThat(result.getCourseNo()).isEqualTo("CS101");
    assertThat(result.getTeacher()).isEqualTo(teacher);
}
```

**What This Tests:**
- ✅ Service checks if course number exists
- ✅ Service creates course with correct data
- ✅ Service associates teacher with course
- ❌ Does NOT test database operations (that's integration test's job)

---

## Integration Testing

### What is Integration Testing?

Integration tests verify that **multiple components work together correctly** using real databases and Spring context.

### File Locations
```
src/test/java/.../controller/
├── AuthControllerIntegrationTest.java         (2 tests)
├── CourseControllerIntegrationTest.java       (2 tests)
└── RegistrationControllerIntegrationTest.java (2 tests)

src/test/java/.../repository/
└── RegistrationRepositoryIntegrationTest.java (2 tests)
```

### Key Concepts

#### 1. Spring Boot Test Setup

```java
@SpringBootTest              // Load full Spring application context
@AutoConfigureMockMvc        // Auto-configure MockMvc for HTTP testing
@ActiveProfiles("test")      // Use application-test.properties
@Transactional               // Rollback database after each test
class AuthControllerIntegrationTest {
    
    @Autowired               // Inject real beans from Spring
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private UserRepository userRepository;
}
```

#### 2. Test Database Configuration

**File:** `src/test/resources/application-test.properties`
```properties
# Use H2 in-memory database (not real PostgreSQL)
spring.datasource.url=jdbc:h2:mem:testdb;MODE=PostgreSQL
spring.datasource.driverClassName=org.h2.Driver

# Create fresh schema for each test run
spring.jpa.hibernate.ddl-auto=create-drop

# JWT settings for testing
app.jwt.secret=test-secret-key-for-testing
```

**Why H2?**
- ✅ In-memory (fast)
- ✅ Compatible with PostgreSQL syntax
- ✅ No external database needed
- ✅ Fresh state for each test

#### 3. Integration Test Flow

```
┌───────────────────────────────────────────────────────┐
│              INTEGRATION TEST FLOW                    │
├───────────────────────────────────────────────────────┤
│                                                       │
│  Test Method                                          │
│      │                                                │
│      ▼                                                │
│  ┌─────────┐   HTTP    ┌────────────┐                │
│  │ MockMvc │ ────────→ │ Controller │ (Real)         │
│  └─────────┘           └──────┬─────┘                │
│                               │                       │
│                               ▼                       │
│                        ┌─────────┐                    │
│                        │ Service │ (Real)             │
│                        └────┬────┘                    │
│                             │                         │
│                             ▼                         │
│                      ┌────────────┐                   │
│                      │ Repository │ (Real)            │
│                      └─────┬──────┘                   │
│                            │                          │
│                            ▼                          │
│                   ┌─────────────────┐                 │
│                   │  H2 In-Memory   │                 │
│                   │    Database     │                 │
│                   └─────────────────┘                 │
│                                                       │
└───────────────────────────────────────────────────────┘
```

### Example: AuthControllerIntegrationTest

```java
@Test
void register_ShouldCreateUserInDatabase() throws Exception {
    RegisterRequest request = new RegisterRequest("student1", "pass123", Role.STUDENT);
    
    // Perform HTTP POST request
    mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())           // HTTP 201
            .andExpect(jsonPath("$.username").value("student1"))
            .andExpect(jsonPath("$.role").value("STUDENT"));
    
    // Verify data was actually saved to database
    User user = userRepository.findByUsername("student1").get();
    assertThat(user).isNotNull();
}
```

**What This Tests:**
- ✅ HTTP request handling
- ✅ JSON serialization/deserialization
- ✅ Controller → Service → Repository flow
- ✅ Database persistence
- ✅ Transaction management

### Unit vs Integration Test Comparison

| Aspect | Unit Test | Integration Test |
|--------|-----------|------------------|
| **Scope** | Single class | Multiple components |
| **Dependencies** | Mocked (@Mock) | Real (H2 database) |
| **Spring Context** | No | Yes (@SpringBootTest) |
| **Speed** | Very fast (10-50ms) | Slower (200-500ms) |
| **Purpose** | Test business logic | Test component interaction |
| **Example** | UserServiceTest | AuthControllerIntegrationTest |

---

## CI/CD Pipeline

### What is CI/CD?

- **CI (Continuous Integration)**: Automatically build and test code on every push
- **CD (Continuous Deployment)**: Automatically deploy passing builds

### Implementation

**File:** `.github/workflows/ci.yml`

```yaml
name: CI Pipeline

on:
  push:
    branches: [ main, develop ]      # Trigger on push
  pull_request:
    branches: [ main, develop ]      # Trigger on PR

jobs:
  test:
    name: Build and Test
    runs-on: ubuntu-latest           # GitHub provides Ubuntu VM
    
    steps:
      - name: Checkout code
        uses: actions/checkout@v4     # Download repository code
        
      - name: Set up JDK 21
        uses: actions/setup-java@v4   # Install Java 21
        with:
          java-version: '21'
          distribution: 'temurin'
          cache: maven                # Cache Maven dependencies
          
      - name: Build and Test
        run: mvn clean verify -B      # Run all tests
        
      - name: Upload test results
        if: always()                  # Upload even if tests fail
        uses: actions/upload-artifact@v4
        with:
          name: test-results
          path: target/surefire-reports/
```

### How CI/CD Works

```
┌─────────────────────────────────────────────────────┐
│                 CI/CD WORKFLOW                      │
├─────────────────────────────────────────────────────┤
│                                                     │
│  Developer                                          │
│     │                                               │
│     │ 1. Write code                                 │
│     │ 2. Commit changes                             │
│     │ 3. git push                                   │
│     ▼                                               │
│  ┌──────────┐                                       │
│  │  GitHub  │ Webhook triggered                     │
│  │   Repo   │                                       │
│  └────┬─────┘                                       │
│       │                                             │
│       ▼                                             │
│  ┌──────────────────────────────────────────────┐  │
│  │     GitHub Actions (Ubuntu VM)              │  │
│  ├──────────────────────────────────────────────┤  │
│  │  1. ✓ Checkout code                         │  │
│  │  2. ✓ Install JDK 21                        │  │
│  │  3. ⚙ mvn clean verify                      │  │
│  │      ├─ Compile code                        │  │
│  │      ├─ Run 6 unit tests                    │  │
│  │      └─ Run 8 integration tests             │  │
│  │  4. ✓ Upload test reports                   │  │
│  └──────────────────────────────────────────────┘  │
│       │                                             │
│       ▼                                             │
│  ┌──────────┐     ┌──────────┐                     │
│  │  ✓ PASS  │ or  │  ✗ FAIL  │                     │
│  └──────────┘     └──────────┘                     │
│                                                     │
└─────────────────────────────────────────────────────┘
```

### Maven Build Lifecycle

```
mvn clean verify
    │
    ├── clean              → Delete target/ folder
    │
    ├── compile            → Compile main code
    │
    ├── test-compile       → Compile test code
    │
    ├── test               → Run unit tests
    │                        (UserServiceTest, CourseServiceTest, etc.)
    │
    ├── integration-test   → Run integration tests
    │                        (AuthControllerIntegrationTest, etc.)
    │
    └── verify             → Verify all tests passed
```

### What Happens on Failure?

```
┌─────────────────────────────────────────┐
│         TEST FAILURE SCENARIO           │
├─────────────────────────────────────────┤
│  1. Developer pushes code               │
│  2. CI runs tests                       │
│  3. ❌ Test fails                        │
│  4. ❌ Build marked as FAILED            │
│  5. 🔴 Red X appears on GitHub           │
│  6. 🚫 Cannot merge PR                   │
│  7. Developer fixes code                │
│  8. Push fix → CI runs again            │
│  9. ✅ Tests pass                        │
│ 10. ✅ Build SUCCESS                     │
│ 11. 🟢 Green checkmark                   │
│ 12. ✅ Can merge PR                      │
└─────────────────────────────────────────┘
```

---

## Branch Protection Rules

### What are Branch Protection Rules?

Rules that protect important branches (like `main`) from direct pushes and enforce quality standards.

### Setup Instructions

1. Go to **GitHub Repository** → **Settings** → **Branches**
2. Click **"Add rule"**
3. Set **Branch name pattern**: `main`
4. Enable the following rules:

### Recommended Rules

| Rule | Purpose | Effect |
|------|---------|--------|
| **Require pull request before merging** | No direct pushes to main | Forces code review process |
| **Require approvals (1+)** | At least 1 reviewer must approve | Ensures peer review |
| **Require status checks to pass** | CI must be green | Prevents broken code from merging |
| **Require branches to be up to date** | Must merge latest main first | Avoids integration conflicts |
| **Include administrators** | Rules apply to everyone | No shortcuts for admins |

### Protected Branch Workflow

```
┌────────────────────────────────────────────────────────┐
│            PROTECTED BRANCH WORKFLOW                   │
├────────────────────────────────────────────────────────┤
│                                                        │
│  Developer Workflow                                    │
│  ─────────────────                                     │
│  1. git checkout -b feature/new-feature                │
│  2. Make changes & commit                              │
│  3. git push origin feature/new-feature                │
│                                                        │
│          │                                             │
│          ▼                                             │
│  ┌────────────────────┐                                │
│  │ Create Pull Request │                               │
│  │  feature → main     │                               │
│  └──────────┬──────────┘                               │
│             │                                          │
│             ▼                                          │
│  ┌─────────────────────────────────────────┐          │
│  │       AUTOMATED CHECKS (Required)       │          │
│  ├─────────────────────────────────────────┤          │
│  │  ☐ CI Pipeline passes (all tests green) │          │
│  │  ☐ Code review approved (1+ reviewer)   │          │
│  │  ☐ Branch is up to date with main       │          │
│  │  ☐ No merge conflicts                   │          │
│  └─────────────────────────────────────────┘          │
│             │                                          │
│             │ All ✅ checks pass                        │
│             ▼                                          │
│  ┌────────────────────┐                                │
│  │  🟢 Merge Allowed   │                               │
│  │   Merge to main     │                               │
│  └────────────────────┘                                │
│                                                        │
└────────────────────────────────────────────────────────┘
```

### What Gets Prevented?

| Action | Without Protection | With Protection |
|--------|-------------------|-----------------|
| **Direct push to main** | ✅ Allowed | ❌ Blocked |
| **Merge without review** | ✅ Allowed | ❌ Blocked |
| **Merge failing tests** | ✅ Allowed | ❌ Blocked |
| **Merge stale branch** | ✅ Allowed | ❌ Blocked |

---

## Complete Workflow

### End-to-End Development Process

```
┌──────────────────────────────────────────────────────────────┐
│                   COMPLETE WORKFLOW                          │
├──────────────────────────────────────────────────────────────┤
│                                                              │
│  LOCAL DEVELOPMENT                                           │
│  ─────────────────                                           │
│  1. Developer creates feature branch                         │
│  2. Writes code + unit tests + integration tests             │
│  3. Runs tests locally: mvn test                             │
│  4. All tests pass ✅                                         │
│  5. Commits changes                                          │
│  6. Pushes branch to GitHub                                  │
│                                                              │
│          │                                                   │
│          ▼                                                   │
│                                                              │
│  GITHUB (Pull Request)                                       │
│  ──────────────────────                                      │
│  7. Developer creates PR: feature → main                     │
│  8. PR triggers CI/CD pipeline                               │
│                                                              │
│          │                                                   │
│          ▼                                                   │
│                                                              │
│  CI/CD PIPELINE (GitHub Actions)                             │
│  ────────────────────────────────                            │
│  9. Checkout code from PR branch                             │
│  10. Setup JDK 21                                            │
│  11. mvn clean verify                                        │
│      ├─ Compile source code                                 │
│      ├─ Compile test code                                   │
│      ├─ Run UserServiceTest                                 │
│      ├─ Run CourseServiceTest                               │
│      ├─ Run RegistrationServiceTest                         │
│      ├─ Run AuthControllerIntegrationTest                   │
│      ├─ Run CourseControllerIntegrationTest                 │
│      ├─ Run RegistrationControllerIntegrationTest           │
│      └─ Run RegistrationRepositoryIntegrationTest           │
│  12. All 14 tests pass ✅                                    │
│  13. Upload test reports                                     │
│                                                              │
│          │                                                   │
│          ▼                                                   │
│                                                              │
│  BRANCH PROTECTION CHECKS                                    │
│  ────────────────────────                                    │
│  14. ✅ CI pipeline passed                                   │
│  15. ✅ Code reviewed by teammate                            │
│  16. ✅ Branch up to date with main                          │
│  17. ✅ No merge conflicts                                   │
│                                                              │
│          │                                                   │
│          ▼                                                   │
│                                                              │
│  18. 🟢 MERGE TO MAIN ✅                                      │
│                                                              │
│          │                                                   │
│          ▼                                                   │
│                                                              │
│  19. Main branch updated                                     │
│  20. CI runs on main (verification)                          │
│  21. Ready for deployment 🚀                                 │
│                                                              │
└──────────────────────────────────────────────────────────────┘
```

---

## Test File Reference

### Unit Tests (Mocked)

| File | Tests | What It Tests |
|------|-------|---------------|
| `UserServiceTest.java` | 2 | User registration logic, duplicate username handling |
| `CourseServiceTest.java` | 2 | Course listing, course creation logic |
| `RegistrationServiceTest.java` | 2 | Student enrollment, duplicate enrollment prevention |

### Integration Tests (Real DB)

| File | Tests | What It Tests |
|------|-------|---------------|
| `AuthControllerIntegrationTest.java` | 2 | User registration API, duplicate username API response |
| `CourseControllerIntegrationTest.java` | 2 | Course listing API, empty list handling |
| `RegistrationControllerIntegrationTest.java` | 2 | Registration persistence, JPA queries |
| `RegistrationRepositoryIntegrationTest.java` | 2 | Custom repository methods |

---

## Quick Commands

```bash
# Run all tests locally
mvn test

# Run specific test class
mvn test -Dtest=UserServiceTest

# Run tests with coverage report
mvn test jacoco:report

# Skip tests (not recommended)
mvn clean install -DskipTests

# View test results
open target/surefire-reports/index.html
```

---

## Benefits Summary

### What We Achieve

1. **Automated Quality Assurance**
   - 14 tests run automatically on every push
   - Catches bugs before code review

2. **Fast Feedback Loop**
   - Tests complete in ~30 seconds
   - Developers know immediately if code breaks

3. **Confidence in Refactoring**
   - Tests ensure existing functionality works
   - Safe to improve code

4. **Documentation**
   - Tests show how code should be used
   - Examples of expected behavior

5. **Team Collaboration**
   - Branch protection enforces code review
   - CI ensures consistent standards

---

## Troubleshooting

### Common Issues

| Issue | Cause | Solution |
|-------|-------|----------|
| Tests pass locally, fail in CI | Different Java version | Use same JDK version (21) |
| H2 database errors | Missing @ActiveProfiles("test") | Add annotation to test class |
| Lombok errors | Version incompatibility | Let Spring Boot manage version |
| Cannot merge PR | CI failed or no approval | Fix tests, get code review |

---

## Conclusion

This system implements industry-standard practices for automated testing and continuous integration:

- ✅ **Unit Tests** isolate and verify business logic
- ✅ **Integration Tests** verify component interactions
- ✅ **CI/CD Pipeline** automates quality checks
- ✅ **Branch Protection** enforces code review and standards

**Result**: Higher code quality, fewer bugs, faster development.
