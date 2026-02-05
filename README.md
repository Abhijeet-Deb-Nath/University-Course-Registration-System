# University Course Registration System

A Spring Boot REST API with JWT authentication, role-based authorization, and Docker support.

## Features
- JWT authentication with `TEACHER` and `STUDENT` roles
- Teachers: create, update, delete courses; view registered students
- Students: view courses, register/drop courses
- PostgreSQL database with JPA/Hibernate
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

## Documentation
- [Authentication & Authorization](authentication_authorization_explained.md)
- [Dockerization](dockerization_explanation.md)
