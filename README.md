# Chess – Development Guide

A two-part project:

- **Backend**: Java Spring Boot REST API for chess game state & move validation.
- **Frontend**: Next.js/React UI for playing and inspecting games.

---

## Prerequisites

- **Java 17 (JDK 17)**
- **Maven Wrapper** (included): `./mvnw`
- **Node.js 18+** and **npm**

> The backend runs on **http://localhost:8080** and the frontend on **http://localhost:3000** by default.

---

## Project Structure

```
src/edu/kingsu/SoftwareEngineering/
├─ Chess_backend/                # Spring Boot API
└─ chess_game_frontEnd/         # Next.js frontend
```

---

## Backend (Spring Boot)

### Run (dev)

```bash
cd src/edu/kingsu/SoftwareEngineering/Chess_backend
./mvnw spring-boot:run
```

### Run tests (backend)

This project uses **JUnit 5** with Maven Surefire.

```bash
# from the backend module
cd src/edu/kingsu/SoftwareEngineering/Chess_backend
./mvnw test
```

**Useful variants**

```bash
# run a single test class
./mvnw -Dtest=BoardTest test

# run a single test method
./mvnw -Dtest=BoardTest#move_basicAndCapture test

# clean then test
./mvnw clean test

# skip tests during build
./mvnw -DskipTests package
```

### REST API – Quick Reference

**Base URL:** `http://localhost:8080`

#### Create a new game

```http
POST /api/game
```

Body: _(none)_  
Response (example):

```json
{
  "gameId": "68feaacf-cc22-4ea2-afef-325f38e58ca2",
  "rev": 0,
  "position": {
    "a2": "wP",
    "e2": "wP",
    "e7": "bP",
    "e1": "wK",
    "e8": "bK",
    "a1": "wR",
    "h8": "bR",
    "...": "..."
  },
  "turn": "WHITE",
  "status": "IN_PROGRESS",
  "lastFrom": null,
  "lastTo": null
}
```

#### Get an existing game

```http
GET /api/game/{id}
```

Response: same shape as above.

#### Make a move

```http
POST /api/game/{id}/move
Content-Type: application/json
```

Body:

```json
{
  "from": "e2",
  "to": "e4",
  "promotion": null,
  "clientRev": 0
}
```

Notes:

- `from`/`to` are algebraic squares `a1..h8`.
- `clientRev` **must equal** the current `rev` from the latest game snapshot (optimistic concurrency).
- On invalid input or illegal moves you may receive:
  - `409 CONFLICT` – stale `clientRev` (refresh your snapshot).
  - `422 UNPROCESSABLE_ENTITY` – invalid squares, wrong turn, blocked path, etc.

**cURL examples**

```bash
# Create game
curl -s -X POST http://localhost:8080/api/game | jq

# Get game
curl -s http://localhost:8080/api/game/REPLACE_WITH_ID | jq

# Make a move
curl -s -X POST http://localhost:8080/api/game/REPLACE_WITH_ID/move \
  -H 'Content-Type: application/json' \
  -d '{"from":"e2","to":"e4","promotion":null,"clientRev":0}' | jq
```

---

## Frontend (Next.js)

```bash
cd src/edu/kingsu/SoftwareEngineering/chess_game_frontEnd
npm install
npm run dev
```

Open **http://localhost:3000**.

> The UI expects the backend at `http://localhost:8080`. If your API base URL differs, update the frontend hooks/services accordingly.

---

## Installing prerequisites

If you don't already have the tools, here are reliable ways to install them:

- **Node.js & npm**
  - Official downloads: https://nodejs.org
  - Recommended (macOS/Linux): **nvm** – https://github.com/nvm-sh/nvm
  - macOS (Homebrew): `brew install node`
- **Maven**
  - Official guide: https://maven.apache.org/install.html
  - macOS (Homebrew): `brew install maven`
- **Java 17 (Temurin/OpenJDK)**
  - Adoptium: https://adoptium.net
  - macOS (Homebrew): `brew install temurin@17`

> Tip: After installing, verify with `node -v`, `npm -v`, `mvn -v`, and `java -version`.
