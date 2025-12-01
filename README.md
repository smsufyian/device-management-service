
# 🚀 Device Management Service

Device Management Service is a Spring Boot REST service for managing devices, backed by PostgreSQL.

![Java](https://img.shields.io/badge/JDK-21-0A7BBB?logo=java&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-6DB33F?logo=spring-boot&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?logo=docker&logoColor=white)
![Database](https://img.shields.io/badge/Database-PostgreSQL-4169E1?logo=postgresql&logoColor=white)

This guide helps first‑time readers understand what the service does, how to run it, how testing and API docs are set up, and where we plan to improve next.

## 📖 What is this service?

Device Management Service provides CRUD and search for devices (e.g., create, update, patch, delete, find/filter). It follows a simple layered architecture:

- API layer: [`DeviceAdministrationController`](src/main/java/com/devices/api/DeviceAdministrationController.java) exposes endpoints and maps DTOs.
- Service layer: [`DeviceService`](src/main/java/com/devices/service/DeviceService.java) holds business rules (validation, state checks, immutable fields, etc.).
- Data layer: Spring Data JPA repositories with Flyway migrations under [`src/main/resources/db/migration`](src/main/resources/db/migration).
- Errors are mapped by [`GlobalExceptionHandler`](src/main/java/com/devices/api/GlobalExceptionHandler.java) to meaningful HTTP responses.

---

## 🧭 Table of Contents

- [📖 What is this service?](#-what-is-this-service)
- [🔎 Device Management Service at a glance](#-devices-api-at-a-glance-domain-capabilities-and-rules)
- [🧰 Prerequisites](#-1-prerequisites)
- [⚡ Quick Start](#-quick-start)
- [📚 API documentation setup](#-api-documentation-setup)
- [🧪 Test architecture](#-test-architecture-honeycomb-model-of-testing)
- [🧹 Run linters](#-run-linters)
- [🐳 Build a Docker image](#-build-a-docker-image)
- [✅ Acceptance criteria](#-acceptance-criteria-how-this-project-satisfies-them)
- [✨ Areas of improvement](#-areas-of-improvement)

---

### 🔎 Device Management at a glance (domain, capabilities, and rules)

- Device domain model
  - Fields: `id`, `name`, `brand`, `state` (`AVAILABLE`, `IN_USE`, `INACTIVE`), `createdAt`, `version`.
  - Code: [`Device.java`](src/main/java/com/devices/domain/Device.java), enum [`DeviceStatus.java`](src/main/java/com/devices/domain/DeviceStatus.java).
  - Persistence: Flyway migrations [`V001__Initial_schema.sql`](src/main/resources/db/migration/V001__Initial_schema.sql), [`V002__create_devices_table.sql`](src/main/resources/db/migration/V002__create_devices_table.sql), [`V003__add_version_column.sql`](src/main/resources/db/migration/V003__add_version_column.sql).

- Supported operations (endpoints)
  - Create a device (POST), full update (PUT), partial update (PATCH), fetch one (GET by id), fetch all (GET), filter by brand/state (GET with filters), delete one (DELETE).
  - Code: API contract [`DeviceAdministrationAPI.java`](src/main/java/com/devices/api/DeviceAdministrationAPI.java), controller [`DeviceAdministrationController.java`](src/main/java/com/devices/api/DeviceAdministrationController.java).
  - DTOs: [`CreateDeviceRequest.java`](src/main/java/com/devices/api/dto/CreateDeviceRequest.java), [`PutDeviceRequest.java`](src/main/java/com/devices/api/dto/PutDeviceRequest.java), [`PatchDeviceRequest.java`](src/main/java/com/devices/api/dto/PatchDeviceRequest.java), [`DeviceFilterRequest.java`](src/main/java/com/devices/api/dto/DeviceFilterRequest.java), [`DeviceResponse.java`](src/main/java/com/devices/api/dto/DeviceResponse.java).
  - Filtering: [`DeviceSpecification.java`](src/main/java/com/devices/repository/DeviceSpecification.java) and [`DeviceRepository.java`](src/main/java/com/devices/repository/DeviceRepository.java).

- Domain validations (where enforced)
  - Creation time cannot be updated: `createdAt` is `@CreatedDate` and `updatable = false` in [`Device`](src/main/java/com/devices/domain/Device.java).
  - When state is `IN_USE`, `name` and `brand` cannot change: see `Device.updateDetails(...)` and `Device.validatePartialUpdate(...)` which throw [`DeviceFieldLockedException`](src/main/java/com/devices/domain/DeviceFieldLockedException.java).
  - Devices in use cannot be deleted: enforced in [`DeviceService`](src/main/java/com/devices/service/DeviceService.java) using [`DeviceInUseException`](src/main/java/com/devices/domain/DeviceInUseException.java) / [`InvalidDeviceStateException`](src/main/java/com/devices/domain/InvalidDeviceStateException.java).
  - Concurrency: optimistic locking via `@Version` column and [`VersionConflictException`](src/main/java/com/devices/domain/VersionConflictException.java).

- Documentation and validation
  - OpenAPI/Swagger via Springdoc: [`OpenApiConfig.java`](src/main/java/com/devices/config/OpenApiConfig.java), properties in [`application.properties`](src/main/resources/application.properties).

---
## 🧰 1) Prerequisites

Required tools:

- [Docker Desktop](https://docs.docker.com/get-docker/)
- [Docker Compose](https://docs.docker.com/compose/)

Verify the installations:
```bash
docker --version
docker compose version
```

Notes:
- Java 21 and Gradle are managed by the Gradle Wrapper; manual installation is not required.

## ⚡ Quick Start
```bash
cd device-management-service
# Start a local PostgreSQL (optional, if you don’t have one)
docker compose up -d

# Run the application
./gradlew bootRun
```
   
After startup:
- Swagger UI dashboard: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs

## 📚 API documentation setup

We use Springdoc OpenAPI. URLs are configured in [`src/main/resources/application.properties`](src/main/resources/application.properties):

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

Additional grouping and metadata are defined in [`src/main/java/com/devices/config/OpenApiConfig.java`](src/main/java/com/devices/config/OpenApiConfig.java). Use `@Operation`, `@Parameter`, and `@Schema` on controllers/DTOs to enrich docs. Keep descriptions concise and focused on request/response shapes and error codes.

### ✅ Run tests
```bash
./gradlew test
```

## 🧪 Test architecture (Honeycomb model of testing)

This project follows the [Honeycomb testing model](https://engineering.atspotify.com/2018/01/testing-of-microservices): many fast, reliable tests close to the code, complemented by a smaller number of broader tests.

- Unit/component tests (broad base): Validate domain rules and small logic slices quickly.
- Integration tests with real infrastructure (narrower middle): Under [`src/test/java/com/devices/*`](src/test/java/com/devices) using Testcontainers ([`TestcontainersConfiguration`](src/test/java/com/devices/TestcontainersConfiguration.java)) to run PostgreSQL and exercise repository/service flows end‑to‑end. Examples: [`DeviceCreationTest`](src/test/java/com/devices/DeviceCreationTest.java), [`DeviceFindingTest`](src/test/java/com/devices/DeviceFindingTest.java), [`DeviceDeletionTest`](src/test/java/com/devices/DeviceDeletionTest.java), [`DeviceValidationTest`](src/test/java/com/devices/DeviceValidationTest.java).
- Smoke test (top): [`SmokeTest`](src/test/java/com/devices/SmokeTest.java) ensures the app boots and basic wiring works.

Run everything with `./gradlew test` (CI does the same).

### 🧹 Run linters
```bash
./gradlew check
```

### 🐳 Build a Docker image
```bash
./gradlew jibDockerBuild
```

## ✅ Acceptance criteria (how this project satisfies them)

- The application should compile and run successfully
  - Build and run with Gradle: `./gradlew bootRun` (see Quick Start). Build config in [`build.gradle.kts`](build.gradle.kts).
- The application must contain a reasonable test coverage
  - Unit/integration tests under `src/test/java/com/devices/*` using Testcontainers (`TestcontainersConfiguration`). Run via `./gradlew test`.
- The API must be documented
  - Springdoc OpenAPI configuration in [`OpenApiConfig.java`](src/main/java/com/devices/config/OpenApiConfig.java) with Swagger UI at `/swagger-ui.html` and docs at `/v3/api-docs` (see [`application.properties`](src/main/resources/application.properties)).
- The application must persist resources to a real database (non in-memory)
  - PostgreSQL configured in [`application.properties`](src/main/resources/application.properties); schema managed with Flyway migrations in [`src/main/resources/db/migration`](src/main/resources/db/migration).
- The application must be containerized
  - Container image built via Jib: `./gradlew jibDockerBuild`. Local infra via [`compose.yaml`](compose.yaml) for PostgreSQL.
- The project must be delivered as a git repository
  - This README and standard Gradle/Jib configuration files are included; project is git-friendly.
- The project includes a README with all necessary instructions
  - This document provides overview, Quick Start, API docs, testing, acceptance mapping, and improvement ideas.

## ✨ Areas of improvement

1) Pagination on GET endpoints
- Add `page`, `size`, and optional `sort` to listing/search endpoints to avoid large payloads and improve performance.
- Return a paged response with `content`, `totalElements`, `totalPages`, and `page/size` metadata.

2) Idempotent APIs
- Make create/update operations safe for retries to prevent duplicates.
- Options:
  - Idempotency keys for POST (client sends a unique header; server deduplicates for a TTL).
  - Prefer PUT for upserts when the resource ID is known.
  - Use optimistic locking (already present via `@Version` and `VersionConflictException`) to guard concurrent updates.

3) API consistency & discoverability
- Standardize error payloads and problem details across endpoints; ensure examples in OpenAPI.
- Add basic rate limits and request validation to reduce noisy traffic.
