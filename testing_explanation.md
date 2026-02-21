# Testing Documentation - University Course Registration System

## Overview

This document explains the testing strategy implemented in the University Course Registration System, demonstrating industry-standard testing practices including **Unit Testing** and **Integration Testing**.

---

## ⚠️ Critical Understanding: Mock vs Real Database

### **Unit Tests - Everything Mocked (No Real Database)**
- Uses **Mockito** to simulate database behavior
- **No real SQL queries executed**
- Tests business logic in isolation
- Extremely fast (milliseconds)
- Located: `src/test/java/.../service/`

### **Integration Tests - Real Database (H2 In-Memory)**
- Uses **H2 in-memory database** (real SQL execution)
- **Real Spring context, real JPA, real transactions**
- Tests full stack from HTTP to database
- Validates SQL queries, constraints, relationships
- Located: `src/test/java/.../controller/` and `.../repository/`

### **Why Both Are Essential:**

| What to Test | Use Unit Test (Mock) | Use Integration Test (Real DB) |
|--------------|---------------------|--------------------------------|
| Business logic decisions | ✅ | ❌ |
| Method calls correct dependencies | ✅ | ❌ |
| SQL queries are correct | ❌ | ✅ |
| Database constraints work (unique, foreign keys) | ❌ | ✅ |
| JPA relationships (cascade, fetch) | ❌ | ✅ |
| Transactions commit/rollback | ❌ | ✅ |
| Full HTTP → Database flow | ❌ | ✅ |
| Authentication + Authorization integration | ❌ | ✅ |

**Bottom line:** You CANNOT test database operations with mocks. Integration tests with real databases are mandatory for production-quality code.

---

## Testing Architecture

### Testing Pyramid

Our testing follows the industry-standard **Testing Pyramid** approach:

```
       /\
      /  \    Integration Tests (25%)
     /____\   - Full stack with real database
    /      \  - HTTP requests to responses
   /________\ - Authentication & Authorization
  /          \
 /____________\ Unit Tests (75%)
               - Service layer business logic
               - Isolated with mocked dependencies
               - Fast execution
```

---

## 1. Unit Testing

### What is Unit Testing?

Unit tests verify **individual components in isolation**. They use **mocks** to simulate dependencies, ensuring tests are:
- **Fast** (no database or network calls)
- **Isolated** (one component at a time)
- **Repeatable** (same result every time)

### What Does "Mocking" Mean?

**Mocking** = Creating fake versions of dependencies so you can test one component in isolation.

**Example:** Testing `UserService.register()` which depends on:
- `UserRepository` (database access)
- `PasswordEncoder` (BCrypt hashing)

**Without mocking:**
```java
// Would need:
// - Real database running
// - Real password encoder
// - Database cleanup after test
// Slow and fragile!
```

**With mocking:**
```java
@Mock
private UserRepository userRepository;  // Fake database
@Mock  
private PasswordEncoder passwordEncoder; // Fake encoder

@InjectMocks
private UserService userService;  // Real service using fake dependencies

@Test
void testRegister() {
    // Tell mock what to return
    when(userRepository.existsByUsername("test")).thenReturn(false);
    when(passwordEncoder.encode("pass")).thenReturn("hashed");
    
    // Test the service logic
    User user = userService.register(request);
    
    // Verify service called the mocks correctly
    verify(userRepository).existsByUsername("test");
    verify(userRepository).save(any(User.class));
}
```

**What we're testing:** Does `UserService` make the right decisions and call dependencies correctly?  
**What we're NOT testing:** Does the database actually save? (That's integration test)

### Technologies Used

- **JUnit 5** (Jupiter) - Testing framework
- **Mockito** - Mocking framework (creates fake dependencies)
- **AssertJ** - Fluent assertions

### Unit Tests Implemented

#### 1. UserServiceTest
**File**: `src/test/java/.../service/UserServiceTest.java`

**Purpose**: Tests user registration, authentication, and role-based access control.

**Key Test Cases**:
```java
✓ register_WhenUsernameIsUnique_ShouldCreateUser()
  - Verifies user creation with unique username
  - Mocks: UserRepository, PasswordEncoder
  
✓ register_WhenUsernameAlreadyExists_ShouldThrowConflictException()
  - Tests duplicate username prevention
  - Expects: ResponseStatusException with CONFLICT status
  
✓ getCurrentUser_WhenAuthenticated_ShouldReturnUser()
  - Tests retrieval of currently logged-in user
  - Mocks: SecurityContext, Authentication
  
✓ requireRole_WhenUserHasWrongRole_ShouldThrowForbiddenException()
  - Tests role-based authorization
  - Expects: ResponseStatusException with FORBIDDEN status
```

**Industry Practice Demonstrated**:
- **Arrange-Act-Assert** (AAA) pattern
- **Mocking external dependencies** (database, security context)
- **Testing both happy path and error scenarios**

---

#### 2. CourseServiceTest
**File**: `src/test/java/.../service/CourseServiceTest.java`

**Purpose**: Tests course creation, update, deletion, and ownership validation.

**Key Test Cases**:
```java
✓ createCourse_WhenCourseNoIsUnique_ShouldCreateCourse()
  - Verifies course creation by teacher
  - Validates business rule: unique course number
  
✓ createCourse_WhenCourseNoAlreadyExists_ShouldThrowConflictException()
  - Tests duplicate course number prevention
  
✓ updateCourse_WhenTeacherDoesNotOwnIt_ShouldThrowForbiddenException()
  - Tests ownership-based authorization
  - Only course owner can update
  
✓ deleteCourse_WhenTeacherOwnsIt_ShouldDeleteCourse()
  - Verifies proper deletion with authorization
```

**Industry Practice Demonstrated**:
- **Business logic validation** (unique constraints, ownership)
- **Authorization testing** (who can do what)
- **Negative testing** (what should NOT work)

---

#### 3. RegistrationServiceTest
**File**: `src/test/java/.../service/RegistrationServiceTest.java`

**Purpose**: Tests student course registration and enrollment logic.

**Key Test Cases**:
```java
✓ register_WhenNotAlreadyRegistered_ShouldCreateRegistration()
  - Tests successful course enrollment
  - Validates student can register
  
✓ register_WhenAlreadyRegistered_ShouldThrowConflictException()
  - Prevents duplicate enrollments
  - Business rule: one registration per course
  
✓ drop_WhenRegistrationExists_ShouldDeleteRegistration()
  - Tests course withdrawal
  
✓ getRegistrationsForCourse_WhenTeacherDoesNotOwnCourse_ShouldThrowForbiddenException()
  - Tests data access authorization
  - Teachers can only see their own course rosters
```

**Industry Practice Demonstrated**:
- **Complex business logic testing** (enrollment rules)
- **Multi-service interaction** (UserService + CourseService + RegistrationService)
- **Data integrity** (no duplicate registrations)

---

## 2. Integration Testing

### What is Integration Testing?

Integration tests verify **multiple components working together**, including:
- HTTP request → Controller → Service → Repository → Database
- Authentication & authorization flows
- Transaction management
- **Real database operations** (H2 in-memory, not mocked!)

**Key Difference from Unit Tests:**
- ❌ No mocks - Everything is real
- ✅ Real Spring context loaded
- ✅ Real database (H2) with real SQL queries
- ✅ Real Spring Security, JPA, transactions
- ✅ Tests the ENTIRE system integration

### Why Use Real Database Instead of Mocks?

**You CANNOT test these with mocks:**

1. **SQL Query Correctness**
   ```java
   // Does this JPA query actually work?
   List<Course> findAllByTeacherId(Long teacherId);
   ```
   Mock would just return fake data. Real DB tests the SQL is correct.

2. **Database Constraints**
   ```java
   @Column(unique = true)
   private String username;
   ```
   Mock can't validate unique constraints work. Real DB enforces them.

3. **Relationships & Cascades**
   ```java
   @OneToMany(cascade = CascadeType.REMOVE)
   private List<Registration> registrations;
   ```
   Mock can't test cascade deletes. Real DB executes them.

4. **Transactions**
   ```java
   @Transactional
   public void register() { ... }
   ```
   Mock can't test rollback on error. Real DB validates transaction behavior.

### Technologies Used

- **Spring Boot Test** - Testing support for Spring applications
- **MockMvc** - Simulate HTTP requests without starting a web server
- **H2 Database** - **Real in-memory database** for testing (PostgreSQL mode)
- **JUnit 5** - Testing framework
- **@SpringBootTest** - Loads full Spring context with all beans
- **@Transactional** - Each test runs in a transaction (auto-rollback)

### Test Database Configuration

**File**: `src/test/resources/application-test.properties`
```properties
spring.datasource.url=jdbc:h2:mem:testdb;MODE=PostgreSQL
spring.jpa.hibernate.ddl-auto=create-drop
```

- Uses **H2 in-memory database** instead of PostgreSQL
- Database is **created** before tests, **dropped** after
- Fast and isolated from production data

---

### Integration Tests Implemented

#### 1. AuthControllerIntegrationTest
**File**: `src/test/java/.../controller/AuthControllerIntegrationTest.java`

**Purpose**: Tests the complete authentication flow from HTTP request to database.

**Key Test Cases**:
```java
✓ register_WithValidData_ShouldCreateUser()
  - POST /api/auth/register
  - Verifies user is saved to database
  - Validates JSON response structure
  
✓ register_WithDuplicateUsername_ShouldReturnConflict()
  - Tests duplicate prevention at HTTP level
  - Expects: 409 CONFLICT status
  
✓ login_WithValidCredentials_ShouldReturnJwtToken()
  - POST /api/auth/login
  - Validates JWT token generation
  - Tests password verification
  
✓ login_WithInvalidCredentials_ShouldReturnUnauthorized()
  - Expects: 401 UNAUTHORIZED
```

**What's Tested**:
- ✅ HTTP request/response
- ✅ JSON serialization/deserialization
- ✅ Database persistence
- ✅ Password hashing (BCrypt)
- ✅ JWT token generation
- ✅ Error responses with correct HTTP status codes

---

#### 2. CourseControllerIntegrationTest
**File**: `src/test/java/.../controller/CourseControllerIntegrationTest.java`

**Purpose**: Tests course management with authentication and authorization.

**Key Test Cases**:
```java
✓ createCourse_AsTeacher_ShouldCreateCourse()
  - POST /api/courses with JWT token
  - Validates role-based access (TEACHER only)
  - Persists to database
  
✓ createCourse_AsStudent_ShouldReturnForbidden()
  - Tests authorization: Students cannot create courses
  - Expects: 403 FORBIDDEN
  
✓ createCourse_WithoutAuth_ShouldReturnUnauthorized()
  - Tests authentication requirement
  - Expects: 401 UNAUTHORIZED
  
✓ updateCourse_AsNonOwner_ShouldReturnForbidden()
  - Tests ownership validation
  - Only course owner can update
  
✓ listMyCourses_AsTeacher_ShouldReturnOnlyMyCourses()
  - GET /api/courses/mine
  - Validates data isolation between users
```

**What's Tested**:
- ✅ JWT authentication flow
- ✅ Role-based authorization (TEACHER vs STUDENT)
- ✅ Ownership-based authorization
- ✅ Multi-user scenarios
- ✅ Data isolation

---

#### 3. RegistrationControllerIntegrationTest
**File**: `src/test/java/.../controller/RegistrationControllerIntegrationTest.java`

**Purpose**: Tests the complete student registration workflow.

**Key Test Cases**:
```java
✓ registerForCourse_AsStudent_ShouldCreateRegistration()
  - POST /api/registrations
  - Full transaction: Student → Course → Registration
  - Validates database relationships
  
✓ registerForCourse_AsTeacher_ShouldReturnForbidden()
  - Teachers cannot register as students
  - Role validation
  
✓ registerForCourse_WhenAlreadyRegistered_ShouldReturnConflict()
  - Tests duplicate enrollment prevention
  - Database constraint validation
  
✓ dropCourse_WhenRegistered_ShouldDeleteRegistration()
  - DELETE /api/registrations
  - Validates cascading updates
  
✓ getCourseStudents_AsNonOwnerTeacher_ShouldReturnForbidden()
  - GET /api/courses/{id}/students
  - Teachers can only see their own course rosters
```

**What's Tested**:
- ✅ Multi-entity relationships (Student ↔ Course ↔ Registration)
- ✅ Transaction management
- ✅ Database constraints (unique enrollments)
- ✅ Cascading operations
- ✅ Complex authorization logic

---

#### 4. RegistrationRepositoryIntegrationTest
**File**: `src/test/java/.../repository/RegistrationRepositoryIntegrationTest.java`

**Purpose**: Tests JPA repository methods with real database operations.

**Key Test Cases**:
```java
✓ existsByStudentIdAndCourseId_WhenRegistrationExists_ShouldReturnTrue()
  - Tests custom query method
  - Validates JPA query generation
  
✓ findAllByStudentId_ShouldReturnAllRegistrationsForStudent()
  - Tests query with filtering
  - Validates data isolation
  
✓ cascadeDelete_WhenCourseDeleted_ShouldDeleteRegistrations()
  - Tests JPA cascade operations
  - @OneToMany relationship validation
```

**What's Tested**:
- ✅ Custom JPA query methods
- ✅ Database relationships
- ✅ Cascade operations
- ✅ Entity lifecycle management

---

## Industry Best Practices Demonstrated

### 1. Test Organization
```
src/test/java/
├── service/              # Unit tests
│   ├── UserServiceTest
│   ├── CourseServiceTest
│   └── RegistrationServiceTest
├── controller/           # Integration tests
│   ├── AuthControllerIntegrationTest
│   ├── CourseControllerIntegrationTest
│   └── RegistrationControllerIntegrationTest
└── repository/           # Repository integration tests
    └── RegistrationRepositoryIntegrationTest
```

### 2. Naming Conventions
- Test classes: `[ClassName]Test` or `[ClassName]IntegrationTest`
- Test methods: `methodName_WhenCondition_ThenExpectedResult()`
- Clear, descriptive names explaining what is tested

### 3. Test Structure (AAA Pattern)
```java
@Test
void testName() {
    // Given (Arrange) - Setup test data
    CourseRequest request = new CourseRequest("CS101", "Intro to CS");
    
    // When (Act) - Execute the operation
    Course result = courseService.createCourse(request);
    
    // Then (Assert) - Verify the outcome
    assertThat(result.getCourseNo()).isEqualTo("CS101");
}
```

### 4. Test Isolation
- Each test is **independent**
- `@BeforeEach` sets up clean state
- `@Transactional` rolls back database changes
- No test depends on another test

### 5. Comprehensive Coverage
- ✅ **Happy path** (everything works)
- ✅ **Error scenarios** (validation failures, conflicts)
- ✅ **Edge cases** (empty data, non-existent IDs)
- ✅ **Security** (authentication, authorization)

### 6. Mock vs Real Dependencies

**Unit Tests** - Use Mocks:
```java
@Mock
private UserRepository userRepository;  // Simulated

@InjectMocks
private UserService userService;  // Real, but with mocked dependencies
```

**Integration Tests** - Use Real Components:
```java
@Autowired
private UserRepository userRepository;  // Real JPA repository

@Autowired
private UserService userService;  // Real service with real dependencies
```

---

## Running the Tests

### Run All Tests
```bash
./mvnw test
```

### Run Specific Test Class
```bash
./mvnw test -Dtest=UserServiceTest
```

### Run Tests with Coverage (if configured)
```bash
./mvnw test jacoco:report
```

### Expected Output
```
[INFO] Tests run: 45, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

---

## Test Metrics

| Category | Count | Coverage |
|----------|-------|----------|
| Unit Tests | 24 | Service layer logic |
| Integration Tests | 21 | Full stack + Database |
| **Total Tests** | **45** | **Core functionality** |

### What's Tested
- ✅ User registration & authentication
- ✅ JWT token generation & validation
- ✅ Role-based authorization (TEACHER/STUDENT)
- ✅ Course CRUD operations
- ✅ Ownership-based permissions
- ✅ Student enrollment workflow
- ✅ Database constraints & relationships
- ✅ Error handling & validation
- ✅ HTTP status codes
- ✅ JSON serialization

---

## Why This Matters

### For Development
- **Catch bugs early** before production
- **Refactor confidently** - tests ensure nothing breaks
- **Document behavior** - tests show how code should work

### For Industry
- **Required practice** in professional software development
- **CI/CD prerequisite** - tests must pass before deployment
- **Code quality indicator** - well-tested code is maintainable code

### For This Project
- ✅ Demonstrates understanding of testing fundamentals
- ✅ Shows ability to write testable code
- ✅ Proves code works as expected
- ✅ Ready for CI/CD integration (next step)

---

## Next Steps: CI/CD Integration

These tests are designed to run automatically in a CI/CD pipeline:
1. Developer pushes code to GitHub
2. GitHub Actions triggers test suite
3. Tests run with H2 database
4. If tests pass → Code can be merged
5. If tests fail → Merge is blocked

**See**: `ci_cd_explanation.md` for CI/CD pipeline setup.

---

## Conclusion

This testing implementation demonstrates **industry-standard practices**:
- Comprehensive unit and integration testing
- Proper test isolation and organization
- Real-world scenarios (authentication, authorization, database operations)
- Ready for automated CI/CD pipelines

The testing strategy ensures code quality, catches bugs early, and provides confidence that the system works as intended.
