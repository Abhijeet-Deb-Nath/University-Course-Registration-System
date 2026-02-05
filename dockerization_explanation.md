# Dockerization Explanation

This document explains the concept of Docker containerization and how it was implemented in this project.

---

## What is Docker?

**Docker** is a platform that packages applications and their dependencies into lightweight, portable containers. Containers run consistently across different environments (development, testing, production) regardless of the underlying infrastructure.

### Key Concepts

| Concept | Description |
|---------|-------------|
| **Image** | A read-only template containing application code, runtime, libraries, and dependencies |
| **Container** | A running instance of an image |
| **Dockerfile** | Instructions to build an image |
| **Docker Compose** | Tool to define and run multi-container applications |
| **Volume** | Persistent storage that survives container restarts |

---

## Why Dockerize?

1. **Consistency** - "Works on my machine" problem solved
2. **Isolation** - Each service runs in its own container
3. **Portability** - Deploy anywhere Docker runs
4. **Scalability** - Easy to replicate containers
5. **Simplified Setup** - One command to start everything

---

## Implementation

### 1. Dockerfile (Multi-Stage Build)

```dockerfile
FROM maven:3.9-eclipse-temurin-24 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -q -DskipTests package

FROM eclipse-temurin:24-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java","-jar","app.jar"]
```

#### Explanation:

**Stage 1 - Build Stage:**
- Uses Maven image with JDK 24
- Copies `pom.xml` and source code
- Builds the JAR file with `mvn package`

**Stage 2 - Runtime Stage:**
- Uses lightweight JRE-only image
- Copies only the built JAR from stage 1
- Exposes port 8082
- Runs the application

**Benefits of Multi-Stage Build:**
- Final image is smaller (no Maven, no source code)
- Build tools don't bloat the production image
- Faster deployment and reduced attack surface

---

### 2. Docker Compose

```yaml
services:
  db:
    image: postgres:16
    environment:
      POSTGRES_DB: course_registration_db
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: 2107118
    ports:
      - "5432:5432"
    volumes:
      - db_data:/var/lib/postgresql/data

  app:
    build: .
    depends_on:
      - db
    environment:
      DB_URL: jdbc:postgresql://db:5432/course_registration_db
      DB_USERNAME: postgres
      DB_PASSWORD: 2107118
      JWT_SECRET: change-this-secret-to-a-long-random-string
      JWT_EXPIRATION_SECONDS: 3600
    ports:
      - "8082:8082"

volumes:
  db_data:
```

#### Explanation:

| Service | Purpose |
|---------|---------|
| `db` | PostgreSQL database container |
| `app` | Spring Boot application container |

**Key Features:**

1. **`depends_on`** - Ensures database starts before the app
2. **Environment Variables** - Configuration passed at runtime
3. **Named Volume (`db_data`)** - Database persists across restarts
4. **Port Mapping (`8082:8082`)** - Maps container port to host port
5. **Service Discovery** - App connects to `db` hostname (Docker DNS)

---

### 3. Application Configuration

```properties
spring.datasource.url=${DB_URL:jdbc:postgresql://localhost:5432/course_registration_db}
spring.datasource.username=${DB_USERNAME:postgres}
spring.datasource.password=${DB_PASSWORD:2107118}
```

**How it works:**
- Uses Spring's `${VAR:default}` syntax
- In Docker: reads from environment variables set in docker-compose.yml
- Locally: falls back to default values

---

## How to Run

### Start Everything
```bash
docker-compose up --build
```

### Start in Background
```bash
docker-compose up -d --build
```

### Stop Everything
```bash
docker-compose down
```

### Stop and Remove Data
```bash
docker-compose down -v
```

### View Logs
```bash
docker-compose logs -f app
```

---

## Architecture Diagram

```
┌─────────────────────────────────────────────────────┐
│                    Docker Host                       │
│                                                      │
│  ┌──────────────┐         ┌──────────────────────┐  │
│  │   db         │         │   app                │  │
│  │  (Postgres)  │◄───────►│  (Spring Boot)       │  │
│  │  Port: 5432  │   db    │  Port: 8082          │  │
│  └──────────────┘         └──────────────────────┘  │
│         │                          │                 │
│         ▼                          ▼                 │
│  ┌──────────────┐         ┌──────────────────────┐  │
│  │  db_data     │         │  Host Port: 8082     │  │
│  │  (Volume)    │         │  → localhost:8082    │  │
│  └──────────────┘         └──────────────────────┘  │
└─────────────────────────────────────────────────────┘
```

---

## Summary

| File | Purpose |
|------|---------|
| `Dockerfile` | Defines how to build the app image |
| `docker-compose.yml` | Orchestrates app + database containers |
| `application.properties` | Reads config from environment variables |

This setup demonstrates containerization fundamentals: multi-stage builds, service orchestration, environment configuration, persistent volumes, and container networking.
