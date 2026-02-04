# University Course Registration System - Complete Project Explanation

## Table of Contents
1. [What is This Project?](#what-is-this-project)
2. [Technology Stack Overview](#technology-stack-overview)
3. [Architecture & Design Patterns](#architecture--design-patterns)
4. [Project Structure Explained](#project-structure-explained)
5. [How the Application Starts](#how-the-application-starts)
6. [Data Flow: From Request to Response](#data-flow-from-request-to-response)
7. [Domain Layer: Your Data Models](#domain-layer-your-data-models)
8. [Repository Layer: Database Access](#repository-layer-database-access)
9. [Service Layer: Business Logic](#service-layer-business-logic)
10. [Controller Layer: HTTP Endpoints](#controller-layer-http-endpoints)
11. [DTO Layer: Data Transfer Objects](#dto-layer-data-transfer-objects)
12. [Frontend: HTML & JavaScript](#frontend-html--javascript)
13. [Database Schema](#database-schema)
14. [Complete Request Lifecycle Examples](#complete-request-lifecycle-examples)
15. [Error Handling Strategy](#error-handling-strategy)
16. [Key Concepts Explained](#key-concepts-explained)

---

## What is This Project?

This is a **University Course Registration System** - a full-stack web application that allows:

- **Students** to:
  - Register accounts
  - Browse available courses
  - Enroll in courses
  - View their registered courses

- **Teachers** to:
  - Register accounts
  - Create and manage courses
  - View students enrolled in their courses
  - Update or delete their courses

Think of it as a simplified version of the online course enrollment system your university uses.

---

## Technology Stack Overview

### Backend Technologies

#### 1. **Java 25**
The programming language used for the entire backend.

#### 2. **Spring Boot 4.0.2**
A framework that makes building Java web applications much easier by:
- Providing auto-configuration (sensible defaults)
- Managing dependencies
- Embedding a web server (Tomcat)
- Handling database connections
- Managing security

**Why Spring Boot?**
Without Spring Boot, you'd need to manually:
- Configure a web server
- Set up database connection pools
- Write boilerplate code for dependency injection
- Configure security filters

Spring Boot does all this automatically!

#### 3. **Spring Data JPA (Java Persistence API)**
- Simplifies database operations
- Converts Java objects to database tables (ORM - Object Relational Mapping)
- Generates SQL queries automatically

**Example:**
```java
User user = userRepository.findByUsername("john_doe");
```
Spring generates: `SELECT * FROM users WHERE username = 'john_doe'`

#### 4. **Spring Security**
- Handles authentication (who are you?)
- Handles authorization (what can you do?)
- Protects endpoints from unauthorized access
- Integrates with JWT tokens

#### 5. **PostgreSQL**
The database where all data is stored:
- User accounts
- Courses
- Course registrations

#### 6. **JWT (JSON Web Tokens)**
A secure way to authenticate users:
- User logs in → receives a token
- User includes token in subsequent requests
- Server validates token to identify user

#### 7. **Maven**
A build tool that:
- Manages dependencies (external libraries)
- Compiles Java code
- Runs tests
- Packages the application

All dependencies are defined in `pom.xml`.

### Frontend Technologies

#### 1. **HTML5**
Defines the structure of web pages:
- `index.html` - Landing page
- `register.html` - Registration form
- `student.html` - Student dashboard
- `teacher.html` - Teacher dashboard

#### 2. **CSS3**
Styles the web pages (`styles.css`).

#### 3. **Vanilla JavaScript**
Handles client-side logic (`app.js`):
- Sending HTTP requests to backend
- Storing JWT tokens in localStorage
- Displaying data dynamically
- Form validation

**Why no React/Angular/Vue?**
This project uses vanilla JavaScript to keep it simple and focused on backend concepts.

---

## Architecture & Design Patterns

### Layered Architecture

Your application follows a **layered (n-tier) architecture**:

```
┌─────────────────────────────────────────┐
│         Presentation Layer              │
│    (HTML, CSS, JavaScript - Browser)    │
└────────────────┬────────────────────────┘
                 │ HTTP Requests
                 ↓
┌─────────────────────────────────────────┐
│         Controller Layer                │
│    (@RestController - HTTP Endpoints)   │
│  - AuthController                       │
│  - CourseController                     │
│  - RegistrationController               │
└────────────────┬────────────────────────┘
                 │ Method Calls
                 ↓
┌─────────────────────────────────────────┐
│         Service Layer                   │
│    (@Service - Business Logic)          │
│  - AuthService                          │
│  - UserService                          │
│  - CourseService                        │
│  - RegistrationService                  │
└────────────────┬────────────────────────┘
                 │ Method Calls
                 ↓
┌─────────────────────────────────────────┐
│         Repository Layer                │
│    (@Repository - Database Access)      │
│  - UserRepository                       │
│  - CourseRepository                     │
│  - RegistrationRepository               │
└────────────────┬────────────────────────┘
                 │ SQL Queries
                 ↓
┌─────────────────────────────────────────┐
│            Database                     │
│         (PostgreSQL)                    │
│  Tables: users, courses, registrations  │
└─────────────────────────────────────────┘
```

**Why layers?**

1. **Separation of Concerns**: Each layer has ONE responsibility
2. **Maintainability**: Changes in one layer don't affect others
3. **Testability**: You can test each layer independently
4. **Reusability**: Services can be used by multiple controllers

### Design Patterns Used

#### 1. **MVC (Model-View-Controller)**
- **Model**: Domain entities (User, Course, Registration)
- **View**: HTML pages
- **Controller**: REST controllers

#### 2. **Repository Pattern**
Abstracts database access:
```java
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
}
```

#### 3. **Dependency Injection (DI)**
Spring creates and manages objects:
```java
public class UserService {
    private final UserRepository userRepository;
    
    // Spring injects UserRepository automatically
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
}
```

**Benefits:**
- Loose coupling
- Easy to test (can inject mocks)
- Spring manages object lifecycle

#### 4. **DTO (Data Transfer Object) Pattern**
Separates internal models from API contracts:
- `User` entity (internal) has `passwordHash`
- `UserSummary` DTO (external) doesn't expose password

---

## Project Structure Explained

```
University Course Registration System/
│
├── src/main/java/                         # Java source code
│   └── com/example/universitycourseregistrationsystem/
│       ├── UniversityCourseRegistrationSystemApplication.java  # Entry point
│       │
│       ├── controller/                    # HTTP endpoint handlers
│       │   ├── AuthController.java        # /api/auth/* endpoints
│       │   ├── CourseController.java      # /api/courses/* endpoints
│       │   └── RegistrationController.java # /api/registrations/* endpoints
│       │
│       ├── service/                       # Business logic
│       │   ├── AuthService.java           # Login logic
│       │   ├── UserService.java           # User management
│       │   ├── CourseService.java         # Course management
│       │   └── RegistrationService.java   # Enrollment logic
│       │
│       ├── repository/                    # Database access
│       │   ├── UserRepository.java        # User CRUD operations
│       │   ├── CourseRepository.java      # Course CRUD operations
│       │   └── RegistrationRepository.java # Registration CRUD operations
│       │
│       ├── domain/                        # Entity models (database tables)
│       │   ├── User.java                  # User entity
│       │   ├── Course.java                # Course entity
│       │   ├── Registration.java          # Registration entity
│       │   └── Role.java                  # Enum: STUDENT, TEACHER
│       │
│       ├── dto/                           # Data Transfer Objects (API contracts)
│       │   ├── AuthRequest.java           # Login request
│       │   ├── AuthResponse.java          # Login response (with token)
│       │   ├── RegisterRequest.java       # Registration request
│       │   ├── CourseRequest.java         # Create/update course
│       │   ├── CourseResponse.java        # Course data for API
│       │   └── RegistrationResponse.java  # Enrollment data for API
│       │
│       ├── security/                      # Security configuration
│       │   ├── SecurityConfig.java        # Security rules
│       │   ├── JwtService.java            # JWT token generation/validation
│       │   ├── JwtAuthenticationFilter.java # Intercepts requests
│       │   └── UserPrincipalService.java  # Loads user details
│       │
│       └── exception/                     # Custom exception handlers
│
├── src/main/resources/                    # Configuration & static files
│   ├── application.properties             # App configuration
│   └── static/                            # Frontend files
│       ├── index.html                     # Landing page
│       ├── register.html                  # Registration page
│       ├── student.html                   # Student dashboard
│       ├── teacher.html                   # Teacher dashboard
│       ├── styles.css                     # Styling
│       └── app.js                         # JavaScript logic
│
├── pom.xml                                # Maven dependencies
├── docker-compose.yml                     # Docker setup for PostgreSQL
└── README.md                              # Project documentation
```

---

## How the Application Starts

### Step-by-Step Startup Process

#### 1. **Entry Point: `main()` Method**

```java
@SpringBootApplication
public class UniversityCourseRegistrationSystemApplication {
    public static void main(String[] args) {
        SpringApplication.run(UniversityCourseRegistrationSystemApplication.class, args);
    }
}
```

**What happens here?**

The `@SpringBootApplication` annotation does THREE things:

a. **`@Configuration`**: Marks this as a configuration class
b. **`@EnableAutoConfiguration`**: Spring Boot automatically configures:
   - Embedded Tomcat web server (port 8081)
   - Database connection pool
   - JSON serialization/deserialization
   - Security filters
   - JPA entity scanning

c. **`@ComponentScan`**: Scans all classes in this package and sub-packages for:
   - `@Controller`, `@RestController`
   - `@Service`
   - `@Repository`
   - `@Component`
   - `@Configuration`

#### 2. **Configuration Loading**

Spring reads `application.properties`:

```properties
# Database connection
spring.datasource.url=jdbc:postgresql://localhost:5432/course_registration_db
spring.datasource.username=postgres
spring.datasource.password=2107118

# JPA configuration
spring.jpa.hibernate.ddl-auto=update  # Auto-create/update tables

# JWT configuration
app.jwt.secret=change-this-secret...
app.jwt.expiration-seconds=3600  # 1 hour

# Server port
server.port=8081
```

#### 3. **Bean Creation (Dependency Injection Container)**

Spring creates instances (beans) of all components:

**Order of creation:**
1. **Configuration beans** (`SecurityConfig`)
2. **Repository interfaces** (Spring Data JPA creates implementations)
3. **Services** (depend on repositories)
4. **Controllers** (depend on services)
5. **Security filters** (`JwtAuthenticationFilter`)

**Example:**
```java
// Spring creates this automatically
UserRepository userRepository = new UserRepositoryImpl(); // Generated by Spring Data

// Then creates UserService and injects repository
UserService userService = new UserService(userRepository, passwordEncoder);

// Then creates AuthController and injects service
AuthController authController = new AuthController(authService, userService);
```

#### 4. **Database Table Creation**

Because `spring.jpa.hibernate.ddl-auto=update`, Hibernate:
1. Scans `@Entity` classes (User, Course, Registration)
2. Creates corresponding tables if they don't exist
3. Updates schema if entities changed

**Generated tables:**
- `users` (from `User.java`)
- `courses` (from `Course.java`)
- `registrations` (from `Registration.java`)

#### 5. **Security Filter Chain Setup**

Spring Security initializes filters:

```
Browser Request
     ↓
[CORS Filter]
     ↓
[JwtAuthenticationFilter] ← Your custom filter
     ↓
[Spring Security Filter Chain]
     ↓
[Controller]
```

#### 6. **Web Server Starts**

Embedded Tomcat starts listening on **port 8081**:

```
Tomcat started on port 8081
Started UniversityCourseRegistrationSystemApplication in 3.456 seconds
```

#### 7. **Application Ready**

You can now access:
- Frontend: `http://localhost:8081/`
- API: `http://localhost:8081/api/...`

---

## Data Flow: From Request to Response

### Complete Flow Diagram

Let's trace a **student enrolling in a course**:

```
┌──────────────────────────────────────────────────────────────────┐
│  STEP 1: Browser Sends Request                                   │
└──────────────────────────────────────────────────────────────────┘

POST http://localhost:8081/api/registrations
Headers:
  Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
  Content-Type: application/json
Body:
  {"courseId": 5}

                          ↓

┌──────────────────────────────────────────────────────────────────┐
│  STEP 2: Tomcat Receives HTTP Request                           │
└──────────────────────────────────────────────────────────────────┘

Tomcat parses HTTP request and passes to Spring DispatcherServlet

                          ↓

┌──────────────────────────────────────────────────────────────────┐
│  STEP 3: Security Filter Chain                                   │
└──────────────────────────────────────────────────────────────────┘

JwtAuthenticationFilter intercepts request:
  1. Extracts token from "Authorization: Bearer <token>" header
  2. Parses JWT token to get username
  3. Loads user from database via UserPrincipalService
  4. Creates Authentication object
  5. Stores in SecurityContextHolder (thread-local storage)
  
SecurityContextHolder now contains:
  - Username: "john_doe"
  - Role: ROLE_STUDENT
  - Authenticated: true

                          ↓

┌──────────────────────────────────────────────────────────────────┐
│  STEP 4: Controller Method Matched                              │
└──────────────────────────────────────────────────────────────────┘

Spring finds matching controller method:

@PostMapping("/api/registrations")
public RegistrationResponse register(@RequestBody RegistrationRequest request)

Spring converts JSON body to RegistrationRequest object:
  request.courseId() = 5

                          ↓

┌──────────────────────────────────────────────────────────────────┐
│  STEP 5: Controller Delegates to Service                        │
└──────────────────────────────────────────────────────────────────┘

RegistrationController.register() method executes:

public RegistrationResponse register(RegistrationRequest request) {
    Registration registration = registrationService.register(request.courseId());
    return toResponse(registration);
}

                          ↓

┌──────────────────────────────────────────────────────────────────┐
│  STEP 6: Service Layer - Business Logic                         │
└──────────────────────────────────────────────────────────────────┘

RegistrationService.register(courseId) executes:

public Registration register(Long courseId) {
    // Get currently logged-in user
    User student = userService.getCurrentUser();
    
    // Verify user is a student
    userService.requireRole(Role.STUDENT);
    
    // Find the course
    Course course = courseService.getCourseOrThrow(courseId);
    
    // Check if already registered
    if (registrationRepository.existsByStudentAndCourse(student, course)) {
        throw new ResponseStatusException(CONFLICT, "Already registered");
    }
    
    // Create registration
    Registration registration = new Registration();
    registration.setStudent(student);
    registration.setCourse(course);
    
    // Save to database
    return registrationRepository.save(registration);
}

                          ↓

┌──────────────────────────────────────────────────────────────────┐
│  STEP 7: Repository Layer - Database Access                     │
└──────────────────────────────────────────────────────────────────┘

registrationRepository.save(registration) triggers:

SQL: INSERT INTO registrations (student_id, course_id) VALUES (3, 5)

Hibernate executes:
  1. Converts Java object to SQL
  2. Executes INSERT query
  3. Database returns generated ID
  4. Hibernate sets ID on registration object

registration.id = 42

                          ↓

┌──────────────────────────────────────────────────────────────────┐
│  STEP 8: Service Returns to Controller                          │
└──────────────────────────────────────────────────────────────────┘

RegistrationService returns Registration object to controller

                          ↓

┌──────────────────────────────────────────────────────────────────┐
│  STEP 9: Controller Converts to DTO                             │
└──────────────────────────────────────────────────────────────────┘

Controller converts domain object to DTO:

private RegistrationResponse toResponse(Registration r) {
    return new RegistrationResponse(
        r.getId(),                    // 42
        r.getCourse().getId(),        // 5
        r.getCourse().getCourseNo(),  // "CS101"
        r.getCourse().getCourseName(), // "Introduction to Programming"
        r.getStudent().getId(),       // 3
        r.getStudent().getUsername()  // "john_doe"
    );
}

                          ↓

┌──────────────────────────────────────────────────────────────────┐
│  STEP 10: Spring Converts to JSON                               │
└──────────────────────────────────────────────────────────────────┘

Spring's Jackson library converts RegistrationResponse to JSON:

{
  "id": 42,
  "courseId": 5,
  "courseNo": "CS101",
  "courseName": "Introduction to Programming",
  "studentId": 3,
  "studentUsername": "john_doe"
}

                          ↓

┌──────────────────────────────────────────────────────────────────┐
│  STEP 11: HTTP Response Sent                                    │
└──────────────────────────────────────────────────────────────────┘

HTTP/1.1 201 Created
Content-Type: application/json

{
  "id": 42,
  "courseId": 5,
  ...
}

                          ↓

┌──────────────────────────────────────────────────────────────────┐
│  STEP 12: Browser Receives Response                             │
└──────────────────────────────────────────────────────────────────┘

JavaScript in student.html displays success message:
"Successfully enrolled in CS101!"
```

---

## Domain Layer: Your Data Models

### What are Domain Entities?

Domain entities are **Java classes that represent database tables**. Each instance of an entity represents a **row** in the table.

### Entity Annotations Explained

#### `@Entity`
Marks this class as a JPA entity (database table).

#### `@Table(name = "users")`
Specifies the table name in the database.

#### `@Id`
Marks the primary key field.

#### `@GeneratedValue(strategy = GenerationType.IDENTITY)`
Database auto-generates the ID (auto-increment).

#### `@Column`
Configures column properties:
- `nullable = false`: Column cannot be NULL
- `unique = true`: Value must be unique across all rows
- `length = 64`: Maximum string length

#### Lombok Annotations
- `@Getter`: Generates getter methods automatically
- `@Setter`: Generates setter methods automatically
- `@NoArgsConstructor`: Generates no-argument constructor

### User Entity

```java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;                    // Primary key

    @Column(nullable = false, unique = true, length = 64)
    private String username;            // Unique username

    @Column(nullable = false, length = 100)
    private String passwordHash;        // Encrypted password

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private Role role;                  // STUDENT or TEACHER
}
```

**Database table:**
```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(64) NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    role VARCHAR(16) NOT NULL
);
```

**Example rows:**
```
+----+------------+-----------------------------+---------+
| id | username   | password_hash               | role    |
+----+------------+-----------------------------+---------+
| 1  | john_doe   | $2a$10$N9qo8uLOickgx2...      | STUDENT |
| 2  | prof_smith | $2a$10$8dF3gkL1mxKj...        | TEACHER |
+----+------------+-----------------------------+---------+
```

### Role Enum

```java
public enum Role {
    TEACHER,
    STUDENT
}
```

**Why enum?**
- Type-safe (can't assign invalid value)
- Readable in code
- Stored as string in database

### Course Entity

```java
@Entity
@Table(name = "courses")
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 32)
    private String courseNo;            // Course number (e.g., "CS101")

    @Column(nullable = false, length = 120)
    private String courseName;          // Course name

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "teacher_id", nullable = false)
    private User teacher;               // Course instructor

    @OneToMany(mappedBy = "course", cascade = CascadeType.REMOVE)
    private List<Registration> registrations = new ArrayList<>();
}
```

#### Relationship Annotations Explained

**`@ManyToOne`**: Many courses can have one teacher
- A teacher can teach multiple courses
- Each course has exactly one teacher

**`fetch = FetchType.EAGER`**: Load teacher data immediately
- Alternative: `FetchType.LAZY` (load only when accessed)

**`@JoinColumn(name = "teacher_id")`**: Foreign key column name

**`@OneToMany(mappedBy = "course")`**: One course can have many registrations
- `mappedBy = "course"`: The `course` field in `Registration` owns this relationship

**`cascade = CascadeType.REMOVE`**: Delete registrations when course is deleted

**Database table:**
```sql
CREATE TABLE courses (
    id BIGSERIAL PRIMARY KEY,
    course_no VARCHAR(32) NOT NULL UNIQUE,
    course_name VARCHAR(120) NOT NULL,
    teacher_id BIGINT NOT NULL REFERENCES users(id)
);
```

**Example rows:**
```
+----+-----------+--------------------------------+------------+
| id | course_no | course_name                    | teacher_id |
+----+-----------+--------------------------------+------------+
| 1  | CS101     | Introduction to Programming    | 2          |
| 2  | CS201     | Data Structures                | 2          |
| 3  | MATH101   | Calculus I                     | 5          |
+----+-----------+--------------------------------+------------+
```

### Registration Entity

```java
@Entity
@Table(name = "registrations", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"student_id", "course_id"})
})
public class Registration {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;
}
```

**`@UniqueConstraint`**: A student can only register for a course once
- Prevents duplicate enrollments

**Database table:**
```sql
CREATE TABLE registrations (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL REFERENCES users(id),
    course_id BIGINT NOT NULL REFERENCES courses(id),
    UNIQUE (student_id, course_id)
);
```

**Example rows:**
```
+----+------------+-----------+
| id | student_id | course_id |
+----+------------+-----------+
| 1  | 1          | 1         |  -- john_doe enrolled in CS101
| 2  | 1          | 2         |  -- john_doe enrolled in CS201
| 3  | 3          | 1         |  -- jane_doe enrolled in CS101
+----+------------+-----------+
```

### Entity Relationships Visualized

```
┌─────────────┐
│    User     │
│ (TEACHER)   │
└──────┬──────┘
       │
       │ 1:N (One-to-Many)
       │ A teacher can teach multiple courses
       │
       ↓
┌─────────────┐          ┌─────────────────┐
│   Course    │──────────│  Registration   │
└──────┬──────┘   N:M    └────────┬────────┘
       │         (via            │
       │      Registration)      │
       │                         │
       │                         │ N:1 (Many-to-One)
       │                         │ Multiple registrations
       │                         │ for one student
       │                         ↓
       │                  ┌─────────────┐
       └──────────────────│    User     │
              N:M         │  (STUDENT)  │
           (Many-to-Many) └─────────────┘
```

---

## Repository Layer: Database Access

### What is a Repository?

A repository is an **interface** that provides CRUD (Create, Read, Update, Delete) operations for an entity.

**The magic**: You only define the interface, Spring Data JPA generates the implementation!

### UserRepository

```java
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
}
```

#### Inherited Methods (from JpaRepository)

```java
// Save or update
User save(User user);

// Find by ID
Optional<User> findById(Long id);

// Find all
List<User> findAll();

// Delete
void delete(User user);
void deleteById(Long id);

// Count
long count();

// Check existence
boolean existsById(Long id);
```

#### Custom Query Methods

Spring generates SQL based on method names:

**`findByUsername(String username)`**
```sql
SELECT * FROM users WHERE username = ?
```

**`existsByUsername(String username)`**
```sql
SELECT COUNT(*) > 0 FROM users WHERE username = ?
```

#### Method Naming Conventions

- `findBy` → SELECT
- `existsBy` → SELECT COUNT(*) > 0
- `deleteBy` → DELETE
- `countBy` → SELECT COUNT(*)

**Examples:**
```java
// SELECT * FROM users WHERE role = ?
List<User> findByRole(Role role);

// SELECT * FROM users WHERE username LIKE ?
List<User> findByUsernameContaining(String keyword);

// SELECT * FROM users WHERE username = ? AND role = ?
Optional<User> findByUsernameAndRole(String username, Role role);
```

### CourseRepository

```java
public interface CourseRepository extends JpaRepository<Course, Long> {
    Optional<Course> findByCourseNo(String courseNo);
    List<Course> findAllByTeacherId(Long teacherId);
}
```

**Generated SQL:**

**`findByCourseNo(String courseNo)`**
```sql
SELECT * FROM courses WHERE course_no = ?
```

**`findAllByTeacherId(Long teacherId)`**
```sql
SELECT * FROM courses WHERE teacher_id = ?
```

### RegistrationRepository

```java
public interface RegistrationRepository extends JpaRepository<Registration, Long> {
    List<Registration> findAllByStudentId(Long studentId);
    List<Registration> findAllByCourseId(Long courseId);
    boolean existsByStudentAndCourse(User student, Course course);
    Optional<Registration> findByStudentIdAndCourseId(Long studentId, Long courseId);
}
```

**Generated SQL:**

**`existsByStudentAndCourse(User student, Course course)`**
```sql
SELECT COUNT(*) > 0 
FROM registrations 
WHERE student_id = ? AND course_id = ?
```

### Why Repositories?

1. **Abstraction**: Hide database details
2. **Testability**: Easy to mock in unit tests
3. **Consistency**: Uniform API across entities
4. **Type Safety**: Compile-time checking

---

## Service Layer: Business Logic

### What is the Service Layer?

The service layer contains **business logic** - the rules and operations specific to your application.

**Responsibilities:**
- Validate business rules
- Orchestrate multiple repository calls
- Handle transactions
- Enforce authorization

### UserService

```java
@Service
@Transactional(readOnly = true)  // Default: read-only transactions
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // Constructor injection
    public UserService(UserRepository userRepository, 
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional  // Write transaction
    public User register(RegisterRequest request) {
        // Business rule: Username must be unique
        if (userRepository.existsByUsername(request.username())) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT, 
                "Username already exists"
            );
        }
        
        User user = new User();
        user.setUsername(request.username());
        
        // Security: Never store plain-text passwords!
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        
        user.setRole(request.role());
        
        return userRepository.save(user);
    }

    public User getCurrentUser() {
        // Get authentication from Spring Security
        Authentication authentication = SecurityContextHolder
            .getContext()
            .getAuthentication();
        
        if (authentication == null || authentication.getName() == null) {
            throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED, 
                "Unauthorized"
            );
        }
        
        return userRepository.findByUsername(authentication.getName())
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.UNAUTHORIZED, 
                "Unauthorized"
            ));
    }

    public User requireRole(Role role) {
        User user = getCurrentUser();
        
        // Authorization: Check if user has required role
        if (user.getRole() != role) {
            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN, 
                "Access denied"
            );
        }
        
        return user;
    }
}
```

#### Transaction Management

**`@Transactional(readOnly = true)`**: Class-level annotation
- All methods are read-only by default
- Improves performance (database doesn't prepare for writes)

**`@Transactional`**: Method-level annotation
- Overrides class-level setting
- Enables write operations

**Transaction guarantees:**
- **Atomicity**: All operations succeed or all fail
- **Consistency**: Database constraints enforced
- **Isolation**: Concurrent transactions don't interfere
- **Durability**: Committed changes persist

**Example:**
```java
@Transactional
public void transferCredits(Long fromUserId, Long toUserId, int credits) {
    User from = userRepository.findById(fromUserId).orElseThrow();
    User to = userRepository.findById(toUserId).orElseThrow();
    
    from.setCredits(from.getCredits() - credits);
    to.setCredits(to.getCredits() + credits);
    
    userRepository.save(from);
    userRepository.save(to);  // If this fails, both operations rollback
}
```

### CourseService

```java
@Service
@Transactional(readOnly = true)
public class CourseService {
    private final CourseRepository courseRepository;
    private final UserService userService;

    @Transactional
    public Course createCourse(CourseRequest request) {
        // Authorization: Only teachers can create courses
        User teacher = userService.requireRole(Role.TEACHER);
        
        // Business rule: Course number must be unique
        courseRepository.findByCourseNo(request.courseNo()).ifPresent(_ -> {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT, 
                "Course number already exists"
            );
        });
        
        Course course = new Course();
        course.setCourseNo(request.courseNo());
        course.setCourseName(request.courseName());
        course.setTeacher(teacher);  // Set current user as teacher
        
        return courseRepository.save(course);
    }

    @Transactional
    public void deleteCourse(Long courseId) {
        User teacher = userService.requireRole(Role.TEACHER);
        Course course = getCourseOrThrow(courseId);
        
        // Business rule: Teachers can only delete their own courses
        if (!course.getTeacher().getId().equals(teacher.getId())) {
            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN, 
                "Not your course"
            );
        }
        
        // Cascade delete: All registrations are also deleted
        courseRepository.delete(course);
    }
}
```

### RegistrationService

```java
@Service
@Transactional(readOnly = true)
public class RegistrationService {
    private final RegistrationRepository registrationRepository;
    private final CourseService courseService;
    private final UserService userService;

    @Transactional
    public Registration register(Long courseId) {
        // Authorization: Only students can enroll
        User student = userService.requireRole(Role.STUDENT);
        
        Course course = courseService.getCourseOrThrow(courseId);
        
        // Business rule: Can't enroll twice in same course
        if (registrationRepository.existsByStudentAndCourse(student, course)) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT, 
                "Already registered for this course"
            );
        }
        
        Registration registration = new Registration();
        registration.setStudent(student);
        registration.setCourse(course);
        
        return registrationRepository.save(registration);
    }
}
```

### Service Layer Benefits

1. **Centralized Business Logic**: All rules in one place
2. **Reusability**: Multiple controllers can use same service
3. **Testability**: Can test business logic without HTTP layer
4. **Transaction Management**: Ensures data consistency

---

## Controller Layer: HTTP Endpoints

### What is a Controller?

A controller handles **HTTP requests** and returns **HTTP responses**.

**Responsibilities:**
- Map URLs to methods
- Parse request data (JSON, query parameters)
- Call service methods
- Convert domain objects to DTOs
- Return appropriate HTTP status codes

### Controller Annotations

#### `@RestController`
Marks this class as a REST API controller. Combines:
- `@Controller`: Marks as a Spring MVC controller
- `@ResponseBody`: Automatically converts return values to JSON

#### `@RequestMapping("/api/auth")`
Base URL path for all methods in this controller.

#### `@PostMapping("/register")`
Handles POST requests to `/api/auth/register`.

#### `@GetMapping`, `@PutMapping`, `@DeleteMapping`
Handle GET, PUT, DELETE requests respectively.

#### `@RequestBody`
Binds request JSON to method parameter.

#### `@PathVariable`
Extracts value from URL path.

**Example:**
```java
@GetMapping("/courses/{courseId}")
public CourseResponse getCourse(@PathVariable Long courseId) {
    // courseId extracted from URL
}
```

#### `@ResponseStatus(HttpStatus.CREATED)`
Sets HTTP response status to 201 Created.

### AuthController

```java
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final UserService userService;

    /**
     * Register a new user
     * POST /api/auth/register
     * 
     * Request body:
     * {
     *   "username": "john_doe",
     *   "password": "secret123",
     *   "role": "STUDENT"
     * }
     * 
     * Response: 201 Created
     * {
     *   "id": 1,
     *   "username": "john_doe",
     *   "role": "STUDENT"
     * }
     */
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserSummary register(@Valid @RequestBody RegisterRequest request) {
        User user = userService.register(request);
        return toSummary(user);
    }

    /**
     * Login
     * POST /api/auth/login
     * 
     * Request body:
     * {
     *   "username": "john_doe",
     *   "password": "secret123"
     * }
     * 
     * Response: 200 OK
     * {
     *   "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
     *   "type": "Bearer",
     *   "expiresIn": 3600
     * }
     */
    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody AuthRequest request) {
        return authService.login(request);
    }

    private UserSummary toSummary(User user) {
        return new UserSummary(
            user.getId(),
            user.getUsername(),
            user.getRole().name()
        );
    }
}
```

### CourseController

```java
@RestController
@RequestMapping("/api/courses")
public class CourseController {
    private final CourseService courseService;
    private final UserService userService;

    /**
     * List all courses
     * GET /api/courses
     * 
     * No authentication required
     */
    @GetMapping
    public List<CourseResponse> listAll() {
        return courseService.getAllCourses().stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    /**
     * List my courses (teacher only)
     * GET /api/courses/mine
     * 
     * Requires: ROLE_TEACHER
     */
    @GetMapping("/mine")
    @PreAuthorize("hasRole('TEACHER')")
    public List<CourseResponse> listMine() {
        User teacher = userService.getCurrentUser();
        return courseService.getTeacherCourses(teacher.getId()).stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    /**
     * Create a course
     * POST /api/courses
     * 
     * Request body:
     * {
     *   "courseNo": "CS101",
     *   "courseName": "Introduction to Programming"
     * }
     * 
     * Requires: ROLE_TEACHER
     */
    @PostMapping
    @PreAuthorize("hasRole('TEACHER')")
    @ResponseStatus(HttpStatus.CREATED)
    public CourseResponse create(@Valid @RequestBody CourseRequest request) {
        return toResponse(courseService.createCourse(request));
    }

    /**
     * Update a course
     * PUT /api/courses/{courseId}
     * 
     * Requires: ROLE_TEACHER (must be course owner)
     */
    @PutMapping("/{courseId}")
    @PreAuthorize("hasRole('TEACHER')")
    public CourseResponse update(
            @PathVariable Long courseId,
            @Valid @RequestBody CourseRequest request
    ) {
        return toResponse(courseService.updateCourse(courseId, request));
    }

    /**
     * Delete a course
     * DELETE /api/courses/{courseId}
     * 
     * Requires: ROLE_TEACHER (must be course owner)
     */
    @DeleteMapping("/{courseId}")
    @PreAuthorize("hasRole('TEACHER')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long courseId) {
        courseService.deleteCourse(courseId);
    }
}
```

#### `@PreAuthorize("hasRole('TEACHER')")`

Spring Security annotation that:
1. Checks if user is authenticated
2. Checks if user has `ROLE_TEACHER` authority
3. Returns `403 Forbidden` if check fails

**How it works:**
```
Request → JwtAuthenticationFilter → SecurityContextHolder populated
         → @PreAuthorize checks role
         → If authorized: method executes
         → If not: 403 Forbidden returned
```

---

## DTO Layer: Data Transfer Objects

### What are DTOs?

DTOs are **simple data containers** used to transfer data between layers:
- Frontend ↔ Backend
- Controller ↔ Service

**Why not use entities directly?**

1. **Security**: Entity might have sensitive fields (passwordHash)
2. **Flexibility**: API format can differ from database structure
3. **Versioning**: Change API without changing database
4. **Validation**: Different validation rules for API

### Java Records

This project uses **Java Records** for DTOs:

```java
public record AuthRequest(String username, String password) {}
```

**Equivalent to:**
```java
public class AuthRequest {
    private final String username;
    private final String password;
    
    public AuthRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }
    
    public String username() { return username; }
    public String password() { return password; }
    
    // equals(), hashCode(), toString() auto-generated
}
```

**Benefits:**
- Concise syntax
- Immutable by default
- Automatic getters, equals(), hashCode(), toString()

### DTO Examples

#### RegisterRequest

```java
public record RegisterRequest(
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 64)
    String username,
    
    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 100)
    String password,
    
    @NotNull
    Role role
) {}
```

**Validation annotations:**
- `@NotBlank`: Not null, not empty, not whitespace
- `@Size`: Min/max length
- `@NotNull`: Not null

**Usage:**
```json
POST /api/auth/register
{
  "username": "john_doe",
  "password": "secret123",
  "role": "STUDENT"
}
```

#### AuthResponse

```java
public record AuthResponse(
    String token,       // JWT token
    String type,        // "Bearer"
    long expiresIn      // Seconds until expiration
) {}
```

**Usage:**
```json
POST /api/auth/login
Response:
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "expiresIn": 3600
}
```

#### CourseResponse

```java
public record CourseResponse(
    Long id,
    String courseNo,
    String courseName,
    Long teacherId,
    String teacherName
) {}
```

**Mapping from Entity:**
```java
private CourseResponse toResponse(Course course) {
    return new CourseResponse(
        course.getId(),
        course.getCourseNo(),
        course.getCourseName(),
        course.getTeacher().getId(),
        course.getTeacher().getUsername()
    );
}
```

---

## Frontend: HTML & JavaScript

### Frontend Architecture

```
┌─────────────────────────────────────────┐
│  index.html                             │
│  Landing page with login form           │
└──────────────┬──────────────────────────┘
               │ After login
               ↓
       ┌───────────────┐
       │  Role check   │
       └───────┬───────┘
               │
       ├───────┴────────┐
       ↓                ↓
┌─────────────┐   ┌─────────────┐
│student.html │   │teacher.html │
│- View courses│   │- Create course│
│- Enroll      │   │- View students│
└─────────────┘   └─────────────┘
       │                │
       └────────┬───────┘
                ↓
         ┌─────────────┐
         │   app.js    │
         │ API calls   │
         └─────────────┘
```

### app.js - API Client

```javascript
const API_URL = 'http://localhost:8081/api';

// Store JWT token in browser's localStorage
function setToken(token) {
    localStorage.setItem('token', token);
}

function getToken() {
    return localStorage.getItem('token');
}

// Make authenticated API request
async function apiRequest(endpoint, options = {}) {
    const token = getToken();
    const headers = {
        'Content-Type': 'application/json',
        ...(token && { 'Authorization': `Bearer ${token}` })
    };
    
    const response = await fetch(`${API_URL}${endpoint}`, {
        ...options,
        headers
    });
    
    if (!response.ok) {
        throw new Error(await response.text());
    }
    
    return response.json();
}

// Login function
async function login(username, password) {
    const response = await apiRequest('/auth/login', {
        method: 'POST',
        body: JSON.stringify({ username, password })
    });
    
    setToken(response.token);
    return response;
}

// Enroll in course
async function enrollInCourse(courseId) {
    return apiRequest('/registrations', {
        method: 'POST',
        body: JSON.stringify({ courseId })
    });
}
```

### How Frontend Communicates with Backend

#### 1. **Login Flow**

```
User enters username/password in index.html
         ↓
JavaScript calls login(username, password)
         ↓
Sends POST to /api/auth/login
         ↓
Backend validates credentials
         ↓
Backend returns JWT token
         ↓
JavaScript stores token in localStorage
         ↓
Redirect to student.html or teacher.html
```

#### 2. **Authenticated Request Flow**

```
User clicks "Enroll" button
         ↓
JavaScript calls enrollInCourse(courseId)
         ↓
Retrieves token from localStorage
         ↓
Sends POST to /api/registrations
Headers: Authorization: Bearer <token>
Body: {"courseId": 5}
         ↓
Backend validates token
         ↓
Backend creates registration
         ↓
Returns success response
         ↓
JavaScript updates UI
```

---

## Database Schema

### Entity-Relationship Diagram

```
┌─────────────────────────────────────────┐
│               users                     │
├─────────────────────────────────────────┤
│ PK | id           | BIGINT              │
│    | username     | VARCHAR(64) UNIQUE  │
│    | password_hash| VARCHAR(100)        │
│    | role         | VARCHAR(16)         │
└──────────┬──────────────────────────────┘
           │
           │ 1:N (One teacher, many courses)
           │
           ↓
┌─────────────────────────────────────────┐
│              courses                    │
├─────────────────────────────────────────┤
│ PK | id           | BIGINT              │
│    | course_no    | VARCHAR(32) UNIQUE  │
│    | course_name  | VARCHAR(120)        │
│ FK | teacher_id   | BIGINT → users(id)  │
└──────────┬──────────────────────────────┘
           │
           │ 1:N (One course, many registrations)
           │
           ↓
┌─────────────────────────────────────────┐
│          registrations                  │
├─────────────────────────────────────────┤
│ PK | id           | BIGINT              │
│ FK | student_id   | BIGINT → users(id)  │
│ FK | course_id    | BIGINT → courses(id)│
│    | UNIQUE(student_id, course_id)      │
└─────────────────────────────────────────┘
           ↑
           │ N:1 (Many registrations, one student)
           │
┌──────────┴──────────────────────────────┐
│               users                     │
│            (as student)                 │
└─────────────────────────────────────────┘
```

### Sample Data

**users table:**
```
+----+------------+-----------------------------+---------+
| id | username   | password_hash               | role    |
+----+------------+-----------------------------+---------+
| 1  | john_doe   | $2a$10$N9qo8uLO...           | STUDENT |
| 2  | prof_smith | $2a$10$8dF3gkL1...           | TEACHER |
| 3  | jane_doe   | $2a$10$7kM2nP9x...           | STUDENT |
| 4  | prof_jones | $2a$10$5hG8fD2k...           | TEACHER |
+----+------------+-----------------------------+---------+
```

**courses table:**
```
+----+-----------+--------------------------------+------------+
| id | course_no | course_name                    | teacher_id |
+----+-----------+--------------------------------+------------+
| 1  | CS101     | Introduction to Programming    | 2          |
| 2  | CS201     | Data Structures                | 2          |
| 3  | MATH101   | Calculus I                     | 4          |
+----+-----------+--------------------------------+------------+
```

**registrations table:**
```
+----+------------+-----------+
| id | student_id | course_id |
+----+------------+-----------+
| 1  | 1          | 1         |  -- john_doe → CS101
| 2  | 1          | 2         |  -- john_doe → CS201
| 3  | 3          | 1         |  -- jane_doe → CS101
| 4  | 3          | 3         |  -- jane_doe → MATH101
+----+------------+-----------+
```

### SQL Queries Generated

**Find user by username:**
```sql
SELECT * FROM users WHERE username = 'john_doe';
```

**Find all courses taught by a teacher:**
```sql
SELECT * FROM courses WHERE teacher_id = 2;
```

**Find all registrations for a student:**
```sql
SELECT r.*, c.course_no, c.course_name 
FROM registrations r
JOIN courses c ON r.course_id = c.id
WHERE r.student_id = 1;
```

**Check if student already enrolled:**
```sql
SELECT COUNT(*) > 0 
FROM registrations 
WHERE student_id = 1 AND course_id = 1;
```

---

## Complete Request Lifecycle Examples

### Example 1: User Registration

**Step 1: User fills registration form**
```html
<form id="register-form">
  <input name="username" value="john_doe">
  <input name="password" type="password" value="secret123">
  <select name="role"><option value="STUDENT">Student</option></select>
  <button type="submit">Register</button>
</form>
```

**Step 2: JavaScript submits form**
```javascript
document.getElementById('register-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    const formData = new FormData(e.target);
    
    const response = await fetch('http://localhost:8081/api/auth/register', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
            username: formData.get('username'),
            password: formData.get('password'),
            role: formData.get('role')
        })
    });
    
    const user = await response.json();
    console.log('Registered:', user);
});
```

**Step 3: Request reaches Spring Boot**
```
POST /api/auth/register HTTP/1.1
Content-Type: application/json

{
  "username": "john_doe",
  "password": "secret123",
  "role": "STUDENT"
}
```

**Step 4: Spring binds JSON to RegisterRequest**
```java
RegisterRequest request = new RegisterRequest(
    "john_doe",
    "secret123",
    Role.STUDENT
);
```

**Step 5: Controller calls service**
```java
// AuthController.register()
User user = userService.register(request);
```

**Step 6: Service validates and saves**
```java
// UserService.register()
// 1. Check if username exists
if (userRepository.existsByUsername("john_doe")) {
    throw exception;
}

// 2. Create user
User user = new User();
user.setUsername("john_doe");
user.setPasswordHash(passwordEncoder.encode("secret123"));
// Password hashed: $2a$10$N9qo8uLOickgx2...
user.setRole(Role.STUDENT);

// 3. Save to database
return userRepository.save(user);
```

**Step 7: Repository executes SQL**
```sql
INSERT INTO users (username, password_hash, role) 
VALUES ('john_doe', '$2a$10$N9qo8uLO...', 'STUDENT')
RETURNING id;
```

**Step 8: Database returns generated ID**
```
id = 1
```

**Step 9: Service returns User to controller**
```java
User(id=1, username="john_doe", passwordHash="$2a$10...", role=STUDENT)
```

**Step 10: Controller converts to DTO**
```java
UserSummary summary = new UserSummary(
    1,
    "john_doe",
    "STUDENT"
);
```

**Step 11: Spring converts to JSON**
```json
{
  "id": 1,
  "username": "john_doe",
  "role": "STUDENT"
}
```

**Step 12: Response sent to browser**
```
HTTP/1.1 201 Created
Content-Type: application/json

{
  "id": 1,
  "username": "john_doe",
  "role": "STUDENT"
}
```

**Step 13: JavaScript displays success**
```javascript
alert('Registration successful!');
window.location.href = '/index.html';
```

---

### Example 2: Login

**Request:**
```
POST /api/auth/login
{
  "username": "john_doe",
  "password": "secret123"
}
```

**AuthService.login():**
```java
// 1. Authenticate user
Authentication auth = authenticationManager.authenticate(
    new UsernamePasswordAuthenticationToken("john_doe", "secret123")
);
// Spring Security:
//   - Loads user from database via UserPrincipalService
//   - Compares passwords: passwordEncoder.matches(submitted, stored)
//   - If match: returns Authentication object
//   - If not: throws BadCredentialsException

// 2. Generate JWT token
User user = userService.findByUsername("john_doe").orElseThrow();
String token = jwtService.generateToken(
    user.getUsername(),
    Map.of("role", "STUDENT")
);
// Token: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJqb2huX2RvZSIsInJvbGUiOiJTVFVERU5UIiwiaWF0IjoxNjQwMDAwMDAwLCJleHAiOjE2NDAwMDM2MDB9.signature

// 3. Return response
return new AuthResponse(token, "Bearer", 3600);
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "expiresIn": 3600
}
```

**JavaScript stores token:**
```javascript
localStorage.setItem('token', response.token);
```

---

### Example 3: Enrolling in a Course (Authenticated Request)

**Request:**
```
POST /api/registrations
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json

{
  "courseId": 5
}
```

**Step 1: JwtAuthenticationFilter intercepts**
```java
// Extract token from header
String header = request.getHeader("Authorization");
String token = header.substring(7); // Remove "Bearer "

// Parse token
Claims claims = jwtService.parseClaims(token);
String username = claims.getSubject(); // "john_doe"

// Load user details
UserDetails userDetails = userDetailsService.loadUserByUsername(username);

// Create authentication object
Authentication auth = new UsernamePasswordAuthenticationToken(
    userDetails,
    null,
    userDetails.getAuthorities() // [ROLE_STUDENT]
);

// Store in SecurityContextHolder
SecurityContextHolder.getContext().setAuthentication(auth);
```

**Step 2: Request reaches controller**
```java
// RegistrationController.register()
Registration registration = registrationService.register(5);
```

**Step 3: Service processes enrollment**
```java
// RegistrationService.register()
// 1. Get current user from SecurityContextHolder
User student = userService.getCurrentUser(); // john_doe

// 2. Verify student role
userService.requireRole(Role.STUDENT);

// 3. Find course
Course course = courseService.getCourseOrThrow(5);

// 4. Check if already enrolled
if (registrationRepository.existsByStudentAndCourse(student, course)) {
    throw exception;
}

// 5. Create registration
Registration registration = new Registration();
registration.setStudent(student);
registration.setCourse(course);

// 6. Save
return registrationRepository.save(registration);
```

**Step 4: SQL execution**
```sql
-- Check existing enrollment
SELECT COUNT(*) > 0 
FROM registrations 
WHERE student_id = 1 AND course_id = 5;
-- Result: false

-- Insert registration
INSERT INTO registrations (student_id, course_id) 
VALUES (1, 5)
RETURNING id;
-- Result: id = 42
```

**Step 5: Response**
```json
{
  "id": 42,
  "courseId": 5,
  "courseNo": "CS101",
  "courseName": "Introduction to Programming",
  "studentId": 1,
  "studentUsername": "john_doe"
}
```

---

## Error Handling Strategy

### Exception Handling

**ResponseStatusException:**
```java
throw new ResponseStatusException(
    HttpStatus.CONFLICT,      // HTTP status code
    "Username already exists" // Error message
);
```

**Spring automatically converts to:**
```
HTTP/1.1 409 Conflict
Content-Type: application/json

{
  "timestamp": "2026-02-04T10:30:00",
  "status": 409,
  "error": "Conflict",
  "message": "Username already exists",
  "path": "/api/auth/register"
}
```

### Common HTTP Status Codes Used

- **200 OK**: Successful GET, PUT
- **201 Created**: Successful POST (resource created)
- **204 No Content**: Successful DELETE
- **400 Bad Request**: Invalid input (validation failed)
- **401 Unauthorized**: Not logged in
- **403 Forbidden**: Logged in but insufficient permissions
- **404 Not Found**: Resource doesn't exist
- **409 Conflict**: Business rule violation (duplicate username)
- **500 Internal Server Error**: Unexpected error

### Validation Errors

**Invalid request:**
```json
POST /api/auth/register
{
  "username": "ab",
  "password": "123"
}
```

**Response:**
```json
HTTP/1.1 400 Bad Request
{
  "timestamp": "2026-02-04T10:30:00",
  "status": 400,
  "errors": [
    {
      "field": "username",
      "message": "size must be between 3 and 64"
    },
    {
      "field": "password",
      "message": "size must be between 6 and 100"
    }
  ]
}
```

---

## Key Concepts Explained

### 1. Dependency Injection (DI)

**Without DI:**
```java
public class UserService {
    private UserRepository userRepository = new UserRepositoryImpl();
    // Tightly coupled, hard to test
}
```

**With DI:**
```java
public class UserService {
    private final UserRepository userRepository;
    
    // Spring injects dependency
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
}
```

**Benefits:**
- Loose coupling
- Easy to test (inject mocks)
- Spring manages lifecycle

### 2. Singleton Pattern

Spring beans are **singletons** by default:
```java
@Service
public class UserService { }
```

**Only ONE instance** of UserService exists throughout the application lifecycle.

**Why?**
- Memory efficient
- State is shared (but services should be stateless)
- Thread-safe if stateless

### 3. ORM (Object-Relational Mapping)

JPA maps Java objects to database tables:

**Java:**
```java
User user = new User();
user.setUsername("john_doe");
userRepository.save(user);
```

**SQL (generated automatically):**
```sql
INSERT INTO users (username, password_hash, role) 
VALUES ('john_doe', '...', 'STUDENT');
```

### 4. REST API Principles

**Resource-based URLs:**
- `/api/users` - Collection
- `/api/users/1` - Specific resource

**HTTP methods:**
- GET - Read
- POST - Create
- PUT - Update
- DELETE - Delete

**Stateless:**
Each request contains all necessary information (JWT token).

### 5. JWT (JSON Web Tokens)

**Structure:**
```
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.   <- Header
eyJzdWIiOiJqb2huX2RvZSIsInJvbGUiOiJT...   <- Payload
SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_...   <- Signature
```

**Decoded payload:**
```json
{
  "sub": "john_doe",       // Subject (username)
  "role": "STUDENT",       // Custom claim
  "iat": 1640000000,       // Issued at
  "exp": 1640003600        // Expiration
}
```

**How it works:**
1. User logs in
2. Server generates token signed with secret key
3. Client stores token
4. Client includes token in subsequent requests
5. Server validates signature and expiration

**Benefits:**
- Stateless (no session storage on server)
- Secure (cryptographically signed)
- Self-contained (contains user info)

### 6. Transaction Isolation

**Problem:** Concurrent requests might cause data inconsistency

**Solution:** Transactions provide isolation

**Example:**
```java
@Transactional
public void transfer(Long fromId, Long toId, int amount) {
    Account from = accountRepository.findById(fromId).get();
    Account to = accountRepository.findById(toId).get();
    
    from.setBalance(from.getBalance() - amount);
    to.setBalance(to.getBalance() + amount);
    
    accountRepository.save(from);
    accountRepository.save(to);
    
    // If exception occurs, both operations rollback
}
```

### 7. Password Hashing

**Never store plain-text passwords!**

**Hashing process:**
```java
String plainPassword = "secret123";
String hashed = passwordEncoder.encode(plainPassword);
// Result: $2a$10$N9qo8uLOickgx2ZqFq9jG.eK5jL4KQm8L9Y6xZ...
```

**Verification:**
```java
boolean matches = passwordEncoder.matches("secret123", hashed);
// true
```

**Algorithm:** BCrypt
- Salted (random data added)
- Adaptive (can increase cost over time)
- One-way (cannot decrypt)

---

## Summary

Your University Course Registration System is a **professionally architected Spring Boot application** that demonstrates:

1. **Clean Architecture**: Separation of concerns across layers
2. **Security**: JWT authentication, password hashing, role-based authorization
3. **Data Persistence**: JPA entities mapped to PostgreSQL
4. **RESTful API**: Standard HTTP methods and status codes
5. **Dependency Injection**: Spring manages object lifecycle
6. **Transaction Management**: Data consistency guarantees
7. **Validation**: Input validation at multiple levels
8. **Error Handling**: Appropriate HTTP status codes and messages

The data flows from:
**Browser → Controller → Service → Repository → Database**

And back:
**Database → Repository → Service → Controller → Browser**

Each layer has a specific responsibility, making the application maintainable, testable, and scalable.

---

## Next Steps for Learning

1. **Run the application** and trace requests using browser DevTools
2. **Add logging** to see the flow in real-time
3. **Write unit tests** for services
4. **Experiment** with adding new features (e.g., grades, prerequisites)
5. **Read Spring Boot documentation** for deeper understanding

**Congratulations!** You now understand the complete architecture of a modern Spring Boot web application.
