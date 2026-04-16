# Distributed Tic Tac Toe

Distributed Tic Tac Toe is a small Spring Boot microservices project composed of three applications under one repository:

- `game-engine-service`
- `game-session-service`
- `tictactoe-ui`

The game is played automatically by the backend services. The UI starts a simulation, the session service coordinates the game flow, and the engine service validates moves and computes the game outcome.

## Architecture

### 1. `game-engine-service`

The engine service owns the Tic Tac Toe rules.

Responsibilities:

- Maintains in-memory game state keyed by `gameId`
- Validates moves
- Enforces turn order
- Detects wins and draws
- Rejects invalid moves and moves after the game is finished

Configured port:

- `8081`

Implemented endpoints:

- `POST /games/{gameId}/move`
- `GET /games/{gameId}`

Behavior notes:

- A game is created lazily on the first valid move.
- Player `X` must always start.
- The response always includes the full board, current status, winner, move count, and last move.

### 2. `game-session-service`

The session service owns the orchestration of a playable session.

Responsibilities:

- Creates session IDs
- Uses the session ID as the `gameId` in the engine service
- Stores in-memory session state and move history
- Simulates moves for `X` and `O`
- Calls the engine service over REST

Configured port:

- `8082`

Implemented endpoints:

- `POST /sessions`
- `POST /sessions/{sessionId}/simulate`
- `GET /sessions/{sessionId}`

Behavior notes:

- `POST /sessions` creates an empty session.
- `POST /sessions/{sessionId}/simulate` runs the game to completion synchronously.
- The service returns move history, final board, winner, and overall session status.
- The move selector currently chooses a random free cell.

### 3. `tictactoe-ui`

The UI is a Spring Boot MVC application with Thymeleaf and browser-side JavaScript.

Responsibilities:

- Serves the main HTML page
- Calls the session service through a UI-side REST client
- Starts a new simulation
- Replays the returned move history visually on the board
- Shows winner, move count, session ID, and move log

Configured port:

- `8080`

UI endpoints:

- `GET /`
- `POST /api/simulations`
- `GET /api/simulations/{sessionId}`

Behavior notes:

- The UI does not call the session service directly from the browser.
- Instead, the browser talks only to the UI app, and the UI backend proxies session-service operations.
- The session service simulates the whole game in one call.
- The browser replays the returned move history step by step to give a live simulation feel.

## Implementation Notes

### State management

All three applications currently use in-memory state where needed:

- The engine service stores game state in a concurrent in-memory repository.
- The session service stores session state and move history in a concurrent in-memory repository.
- No persistent database is used at this stage.

### Error handling

Each service returns structured JSON error responses.

Examples:

- `404` for unknown game or session IDs
- `409` for invalid game/session state transitions
- `502` when a downstream service cannot be reached

### Inter-service communication

The session service communicates with the engine service using Spring `RestClient`.

The UI communicates with the session service using another `RestClient`.

Default local configuration:

- UI -> Session Service: `http://localhost:8082`
- Session Service -> Engine Service: `http://localhost:8081`

## Project Structure

```text
distributed-tic-tac-toe/
├── pom.xml
├── README.md
├── game-engine-service/
├── game-session-service/
└── tictactoe-ui/
```

This is a Maven multi-module repository at the root level, but each application is also independently runnable from its own module.

## Requirements

Before running locally, make sure you have:

- Java 21
- Maven wrapper support enabled through the included `mvnw` scripts

## How To Run

Run the services in this order.

### 1. Start the game engine service

```bash
cd game-engine-service
./mvnw spring-boot:run
```

Expected local URL:

- `http://localhost:8081`

### 2. Start the game session service

Open a new terminal:

```bash
cd game-session-service
./mvnw spring-boot:run
```

Expected local URL:

- `http://localhost:8082`

### 3. Start the UI

Open a third terminal:

```bash
cd tictactoe-ui
./mvnw spring-boot:run
```

Expected local URL:

- `http://localhost:8080`

### 4. Test through the UI

Open the browser at:

- `http://localhost:8080`

Then:

1. Press `Start Simulation`
2. The UI will create a session through the session service
3. The session service will simulate a full game through the engine service
4. The UI will replay the move history on the board

## REST Flow

The full interaction path is:

1. Browser calls `POST /api/simulations` on the UI
2. UI calls `POST /sessions` on the session service
3. UI calls `POST /sessions/{sessionId}/simulate` on the session service
4. Session service calls `POST /games/{gameId}/move` on the engine service multiple times
5. Engine service validates and returns updated game state after each move
6. Session service returns the final session snapshot with move history
7. UI replays the move history in the browser

## Manual API Testing

You can also test the services without the UI.

### Create a session

```bash
curl -X POST http://localhost:8082/sessions
```

### Simulate a session

Replace `{sessionId}` with the value returned from session creation.

```bash
curl -X POST http://localhost:8082/sessions/{sessionId}/simulate
```

### Fetch a session

```bash
curl http://localhost:8082/sessions/{sessionId}
```

### Fetch the engine-side game directly

Because the session ID is also used as the engine `gameId`, you can inspect the engine state directly:

```bash
curl http://localhost:8081/games/{sessionId}
```

## Testing

Run tests module by module.

### Engine service

```bash
cd game-engine-service
./mvnw test
```

### Session service

```bash
cd game-session-service
./mvnw test
```

### UI

```bash
cd tictactoe-ui
./mvnw test
```

## Current Tradeoffs

This implementation is intentionally simple and local-development friendly.

Current tradeoffs:

- Engine and session state are in memory only
- Data is lost on restart
- Session simulation runs synchronously
- UI replay is based on returned move history, not live push updates
- There is no shared DTO module yet between services

## Possible Next Improvements

- Add a shared contract module for request/response DTOs
- Add persistent storage
- Add SSE or WebSocket streaming for true live updates
- Add a smarter move strategy instead of random moves
- Add Docker support or a `docker-compose.yml`
- Add an API gateway or service discovery layer
- Add end-to-end integration tests across all three services

## Summary

You can now run all three applications locally and test the full flow through the UI:

- Engine validates game rules
- Session service simulates the game through REST
- UI starts the simulation and replays the game visually
