# University Course Registration System

A Spring Boot REST API for managing university course registrations with JWT authentication, comprehensive testing, and automated CI/CD.

## 🚀 Tech Stack

- **Backend**: Spring Boot 3.2, Spring Security, Spring Data JPA
- **Database**: PostgreSQL (Production), H2 (Tests)
- **Auth**: JWT (JSON Web Tokens)
- **Testing**: JUnit 5, Mockito, Spring Boot Test
- **Build**: Maven
- **CI/CD**: GitHub Actions
- **Container**: Docker

## 📁 Project Structure

```
src/
├── main/java/.../universitycourseregistrationsystem/
│   ├── controller/       # REST API endpoints
│   ├── domain/           # JPA entities (User, Course, Registration)
│   ├── dto/              # Request/Response objects
│   ├── repository/       # Data access layer
│   ├── security/         # JWT & Spring Security configuration
│   ├── service/          # Business logic
│   └── exception/        # Custom exceptions
│
└── test/java/            # Automated tests (14 tests total)
    ├── service/          # Unit tests (6 tests with Mockito mocks)
    ├── controller/       # Integration tests (6 tests)
    └── repository/       # Repository tests (2 tests)
```

## 🔌 API Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/auth/register` | Register new user | No |
| POST | `/api/auth/login` | Login & get JWT token | No |
| GET | `/api/courses` | List all courses | No |
| POST | `/api/courses` | Create course | Teacher |
| PUT | `/api/courses/{id}` | Update course | Teacher (owner) |
| DELETE | `/api/courses/{id}` | Delete course | Teacher (owner) |
| GET | `/api/courses/my` | My courses | Teacher |
| POST | `/api/registrations` | Register for course | Student |
| DELETE | `/api/registrations/{courseId}` | Drop course | Student |
| GET | `/api/registrations/mine` | My registrations | Student |
| GET | `/api/registrations/course/{id}/students` | Course students | Teacher (owner) |

## 🏃 Running Locally

### Prerequisites
- JDK 21
- Maven or Docker

### Option 1: Maven
```bash
./mvnw spring-boot:run
```

### Option 2: Docker
```bash
docker-compose up
```

Application runs at: `http://localhost:8080`

## 🧪 Testing

The project includes **14 automated tests** covering unit and integration testing:

```bash
# Run all tests
./mvnw test

# Run specific test
./mvnw test -Dtest=UserServiceTest
```

### Test Coverage
- **Unit Tests** (6): Test business logic with mocked dependencies
- **Integration Tests** (8): Test API endpoints and database with H2

**For detailed testing documentation**, see [TESTING_AND_CI_CD_GUIDE.md](./TESTING_AND_CI_CD_GUIDE.md)

## 🔄 CI/CD Pipeline

GitHub Actions automatically:
1. ✅ Builds the project
2. ✅ Runs all 14 tests
3. ✅ Validates code quality
4. ✅ Prevents merging if tests fail

**Pipeline Status**: ![CI](https://github.com/YOUR_USERNAME/University-Course-Registration-System/workflows/CI%20Pipeline/badge.svg)

**For CI/CD details**, see [TESTING_AND_CI_CD_GUIDE.md](./TESTING_AND_CI_CD_GUIDE.md)

## 🔒 Security

- JWT-based authentication
- Password hashing with BCrypt
- Role-based access control (STUDENT, TEACHER)
- Protected endpoints

## 📚 Documentation

| Document | Description |
|----------|-------------|
| [README.md](./README.md) | Project overview & quick start (this file) |
| [TESTING_AND_CI_CD_GUIDE.md](./TESTING_AND_CI_CD_GUIDE.md) | Detailed guide on testing, CI/CD, branch protection |

## 🔧 Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `SPRING_DATASOURCE_URL` | Database URL | `jdbc:postgresql://localhost:5432/university` |
| `SPRING_DATASOURCE_USERNAME` | DB username | `postgres` |
| `SPRING_DATASOURCE_PASSWORD` | DB password | `password` |
| `APP_JWT_SECRET` | JWT signing key | (must be set, min 32 chars) |
| `APP_JWT_EXPIRATION_SECONDS` | Token validity | `3600` (1 hour) |

## 📦 Docker Setup

```yaml
# docker-compose.yml included
services:
  - postgres (database)
  - app (Spring Boot)
```

```bash
docker-compose up -d
```

## 🤝 Contributing

1. Create feature branch: `git checkout -b feature/new-feature`
2. Write code + tests
3. Commit: `git commit -m "Add new feature"`
4. Push: `git push origin feature/new-feature`
5. Create Pull Request
6. Wait for CI checks to pass ✅
7. Get code review approval
8. Merge to main

## 📄 License

MIT License
