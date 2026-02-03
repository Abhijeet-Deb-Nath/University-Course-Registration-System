# University Course Registration System

Minimal Spring Boot REST API with JWT authentication and role-based authorization.

## Features
- JWT login with roles: `TEACHER` and `STUDENT`
- Teachers can create/update/delete their courses
- Students can view all courses and register/drop
- Teachers can view students registered in their courses only

## Quick Start (local)
1. Start Postgres (Docker Compose is recommended)
2. Run the app with Maven

### Run Postgres with Docker Compose
```bash
docker compose up -d db
```

### Run the API
```bash
./mvnw spring-boot:run
```

## Full Docker Compose
```bash
docker compose up --build
```

## Auth Flow
1. Register user (teacher or student)
2. Login to receive JWT access token
3. Use `Authorization: Bearer <token>` in requests

### Example Requests
Register teacher:
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"alice","password":"password","role":"TEACHER"}'
```

Login:
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"alice","password":"password"}'
```

Create course (teacher only):
```bash
curl -X POST http://localhost:8080/api/courses \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"courseNo":"CSE101","courseName":"Intro to CS"}'
```

Register for course (student only):
```bash
curl -X POST http://localhost:8080/api/registrations \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"courseId":1}'
```

## Local UI
After starting the app, open:
- http://localhost:8081/

Pages:
- http://localhost:8081/index.html
- http://localhost:8081/student.html
- http://localhost:8081/teacher.html

The UI lets you register, login, and call the REST API with your JWT token.

## Notes
- Update `JWT_SECRET` with a long random string before production use.
- Default DB credentials are configured for local Docker Postgres.
