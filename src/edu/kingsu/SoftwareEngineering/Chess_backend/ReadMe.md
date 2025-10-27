

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

-––

## Small example: controllers / services / entities (brief)

**controllers/GameController.java**
- `POST /api/game` → create new game → `{ gameId, board, turn, status }`
- `GET /api/game/{id}` → current state
- `POST /api/game/{id}/move` → apply `{ from, to, promotion? }` → updated state or 400
- `GET /api/game/{id}/legal-moves?from=e2` → legal destinations

They define endpoints such as creating a game, fetching a game by ID, submitting a move, and listing legal moves for a square. Controllers validate and deserialize input, delegate all work to the service layer, and translate domain errors into HTTP responses (for example, 400 for an illegal move or 404 for an unknown game). They return lightweight DTOs shaped for the frontend; no chess rules live here.

**services/**
```java
public interface GameService {
  UUID newGame();
  GameState getState(UUID id);
  GameState makeMove(UUID id, Move move);
  List<Position> legalMoves(UUID id, Position from);
}

// Example in-memory store (thread-safe)
public class InMemoryGameStore {
  private final ConcurrentMap<UUID, GameState> store = new ConcurrentHashMap<>();
  public UUID save(GameState s) { var id = UUID.randomUUID(); store.put(id, s); return id; }
  public Optional<GameState> find(UUID id) { return Optional.ofNullable(store.get(id)); }
  public void update(UUID id, GameState s) { store.put(id, s); }
}
```
Put application logic here. A GameService acts as the façade that controllers call; it is responsible for starting new games, reading state, making moves, and listing legal moves. Under the hood, it coordinates two helpers: an InMemoryGameStore, which keeps GameState objects in a thread‑safe map keyed by UUID (sufficient for development when there is no database), and an optional RulesEngine, which encapsulates chess rules such as validating and applying moves, computing legal moves from a position, and detecting check, checkmate, and stalemate. Keeping rules here keeps controllers thin and the domain easy to test.

**entities/** (plain domain classes; no JPA needed)
```java
public enum Color { WHITE, BLACK }
public enum PieceType { KING, QUEEN, ROOK, BISHOP, KNIGHT, PAWN }
public enum GameStatus { IN_PROGRESS, CHECK, CHECKMATE, STALEMATE, DRAW }

public record Position(int row, int col) {
  public static Position fromAlgebraic(String a) { /* e2 -> (6,4) */ throw new UnsupportedOperationException(); }
}

public record Move(Position from, Position to, PieceType promotion) {}

public class GameState {
  public Board board; public Color turn; public GameStatus status; public List<Move> history;
}
```
Keep plain domain classes here with no framework annotations. Typical classes include GameState (board, whose turn it is, game status, and move history), Board (an 8×8 representation with helpers to read a square and apply a move), Piece (type, color, position), Move (from, to, optional promotion and flags for captures or castling), and Position (row/column or file/rank with helpers to convert algebraic notation like “e2”). Add small enums such as Color, PieceType, and GameStatus. These classes model state and simple invariants; complex behavior stays in the service layer.

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