
A Spring Boot service for device management backed by PostgreSQL. This guide helps first‑time contributors get the project running quickly, run tests and checks, and build Docker images with Jib.

---
## 1) Prerequisites

Required tools:

- [Docker Desktop](https://docs.docker.com/get-docker/)
- [Docker Compose](https://docs.docker.com/compose/)

Verify the installations: 
```bash
docker --version
docker compose version
```

Notes:
- [Java 21 and Gradle are managed by the Gradle Wrapper](gradle/wrapper/gradle-wrapper.properties).Manual installation is not required.

## Quick Start
   ```bash
      cd devices-api
      ./gradlew version
   ```
   
### To run the app locally
  ```bash
      cd devices-api
      ./gradlew bootRun 
  ```

### To run the test  
  ```bash
      cd devices-api
      ./gradlew test
  ```

### To run linters
  ```bash
      cd devices-api
      ./gradlew check
  ```

### To create a docker image
  ```bash
      cd devices-api
      ./gradlew jibDockerBuild 
  ```
