

# Chess Backend (Spring Boot) — Quick Start

This repository includes a Spring Boot backend located at:

```
src/edu/kingsu/SoftwareEngineering/Chess_backend
```

Use this README to get the backend up and running quickly in development.

---

## Prerequisites
- **JDK 17 or newer** (Temurin/Adoptium recommended). Verify with:
  ```bash
  java -version
  ```
- **Build tool**: The project is expected to include **Maven Wrapper (`mvnw`)** or **Gradle Wrapper (`gradlew`)**. If not, install Maven 3.9+ or Gradle 8+ locally.
- (Optional) **Ant** is configured in this repo to start the backend from the project root.

> Default dev port is **8080** unless overridden.

---

## Project Structure (backend)
```
Chess/
└─ src/edu/kingsu/SoftwareEngineering/Chess_backend/
   ├─ src/main/java/...                 # Java source
   ├─ src/main/resources/
   │  └─ application.properties|yml     # Spring Boot configuration
   ├─ pom.xml | build.gradle            # Build definition (Maven or Gradle)
   ├─ mvnw / mvnw.cmd (optional)        # Maven wrapper
   └─ gradlew / gradlew.bat (optional)  # Gradle wrapper
```

---

## Configure (development)
Spring Boot reads configuration from `src/main/resources/application.properties` (or `.yml`). Common entries:
```properties
# Server
server.port=8080
```
If you use environment variables, Spring Boot maps them automatically (e.g., `SERVER_PORT`, `SPRING_DATASOURCE_URL`).

> This repo’s `.gitignore` excludes `.env` files by default; commit a `*.example` instead if needed.

---

## Run the Backend

### Option A — From the repo root via **Ant**
From the project root (where `build.xml` lives):
```bash
ant run-backend
```
This starts the Spring Boot app **in the background** (detached). To bring up the frontend as well, you can use:
```bash
ant run-dev
```

### Option B — From the backend directory via **Maven**(highly recommended as you will be able to see logs and many more)
```bash
cd src/edu/kingsu/SoftwareEngineering/Chess_backend
./mvnw spring-boot:run        # or: mvn spring-boot:run
```

Open the app at:
```
http://localhost:8080
```

---

## Build & Run as JAR

### Maven
```bash
cd src/edu/kingsu/SoftwareEngineering/Chess_backend
./mvnw clean package
java -jar target/*.jar
```
---

## Change the Port
To use a different port in dev:
```bash
# Maven
./mvnw spring-boot:run -Dspring-boot.run.arguments=--server.port=9090
```
Or set `server.port=9090` in `application.properties`.

---

## Logs & Processes
- When run via **Maven/Gradle**, logs appear in the current terminal. Stop with **Ctrl+C**.
- When run via **Ant** (detached), the process keeps running after Ant exits. To find/stop it on macOS:
  ```bash
  sudo lsof -nP -iTCP:8080 -sTCP:LISTEN   # find the PID listening on 8080
  kill <PID>
  ```

---

## Next Steps
- Add controllers/services/entities for game state and moves.
- Define an API contract (e.g., `/api/game/{id}/move`).
- Add integration tests as needed.

---

## Troubleshooting
- **`Port 8080 already in use`**: change `server.port` or stop the process on 8080.
- **`JAVA_HOME` / JDK issues**: ensure a JDK (not just a JRE) is installed and on PATH.
- **Wrapper not executable (macOS/Linux)**: `chmod +x mvnw gradlew`.

---

**Maintainer**: Alain, Kodi, Taylor, Jonathan