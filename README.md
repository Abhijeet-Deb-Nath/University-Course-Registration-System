# University Course Registration System

A Spring Boot REST API with JWT authentication, role-based authorization, Docker support, and CI/CD pipeline.

## Features
- JWT authentication with `TEACHER` and `STUDENT` roles
- Teachers: create, update, delete courses; view registered students
- Students: view courses, register/drop courses
- PostgreSQL database with JPA/Hibernate
- **Comprehensive testing** (45 unit + integration tests)
- **CI/CD pipeline** with GitHub Actions
- Dockerized deployment

## Quick Start

### Option 1: Docker Compose (Recommended)
```bash
docker-compose up --build
```
Access the app at: **http://localhost:8082**

### Option 2: Local Development
1. Start PostgreSQL:
   ```bash
   docker-compose up -d db
   ```
2. Run the application:
   ```bash
   ./mvnw spring-boot:run
   ```

## API Endpoints

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | `/api/auth/register` | Register user | - |
| POST | `/api/auth/login` | Login, get JWT | - |
| GET | `/api/courses` | List all courses | Any |
| POST | `/api/courses` | Create course | Teacher |
| POST | `/api/registrations` | Register for course | Student |
| DELETE | `/api/registrations/{id}` | Drop course | Student |

## Example Usage

**Register:**
```bash
curl -X POST http://localhost:8082/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"alice","password":"password","role":"TEACHER"}'
```

**Login:**
```bash
curl -X POST http://localhost:8082/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"alice","password":"password"}'
```

**Create Course (with JWT):**
```bash
curl -X POST http://localhost:8082/api/courses \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"courseNo":"CSE101","courseName":"Intro to CS"}'
```

## Web UI
Static pages available at:
- `http://localhost:8082/index.html` - Login/Register
- `http://localhost:8082/student.html` - Student dashboard
- `http://localhost:8082/teacher.html` - Teacher dashboard

## Configuration
Environment variables (with defaults in `application.properties`):
- `DB_URL` - Database connection URL
- `DB_USERNAME` / `DB_PASSWORD` - Database credentials
- `JWT_SECRET` - Secret key for JWT signing
- `JWT_EXPIRATION_SECONDS` - Token validity period

## Testing

### Run Tests
```bash
./mvnw test
```

### Test Coverage
- **45 comprehensive tests** (24 unit + 21 integration)
- Unit tests: Service layer business logic with Mockito mocks
- Integration tests: Full stack with H2 in-memory database
- Tests authentication, authorization, CRUD operations, and database constraints

### Test Types
- `*ServiceTest.java` - Unit tests (isolated, fast)
- `*IntegrationTest.java` - Integration tests (full stack, real database)

## CI/CD Pipeline

### GitHub Actions
Automated testing runs on every push and pull request:
- ✅ Compiles code
- ✅ Runs all 45 tests with **H2 in-memory database** (industry standard)
- ✅ Generates test reports
- ✅ Reports pass/fail status to GitHub

**Note:** Tests use H2 (not PostgreSQL) for speed and isolation - this is **industry best practice** for CI/CD.

### Workflow File
`.github/workflows/ci.yml` - Automation configuration (runs tests)

### Branch Protection Rules
**Important:** Rules are configured on **GitHub website**, not in .yml file!

**Setup:** Settings → Branches → Add rule for `main` branch:
- ☑️ Require pull request before merging
- ☑️ Require status checks to pass (select "CI Pipeline")
- ☑️ Require branches to be up to date

**Result:** Direct pushes to main blocked, tests must pass before merge.

**Detailed Guide:** See [branch_protection_setup.md](branch_protection_setup.md)

## Documentation
- [Authentication & Authorization](authentication_authorization_explained.md)
- [Dockerization](dockerization_explanation.md)
- [Testing Guide](testing_explanation.md) - Comprehensive testing explanation
- [Testing Summary](testing_summary.md) - Implementation summary
- [CI/CD Pipeline](ci_cd_explanation.md) - GitHub Actions setup
- [CI/CD Quick Reference](ci_cd_quick_reference.md) - Quick commands
- [Branch Protection Setup](branch_protection_setup.md) - Step-by-step guide

## Technology Stack
- **Backend**: Spring Boot 4.0.2, Java 21
- **Security**: Spring Security, JWT
- **Database**: PostgreSQL (production), H2 (testing)
- **Testing**: JUnit 5, Mockito, Spring Boot Test
- **CI/CD**: GitHub Actions
- **Containerization**: Docker, Docker Compose
