package com.backend.chess_backend.web;

import java.util.Map;

/**
 * DTO representing the authoritative state of a chess game as returned by the API.
 * <p>
 * Fields:
 * </p>
 * <ul>
 *   <li>{@code gameId} – server-generated identifier for the game.</li>
 *   <li>{@code rev} – monotonically increasing revision/version of the position (increments after each accepted move).</li>
 *   <li>{@code position} – map from algebraic square (e.g., "e2") to two-character piece code (e.g., "wP", "bK"). Only occupied squares are present.</li>
 *   <li>{@code turn} – side to move: {@code "WHITE"} or {@code "BLACK"}.</li>
 *   <li>{@code status} – lifecycle status, e.g., {@code "IN_PROGRESS"}.</li>
 *   <li>{@code lastFrom}, {@code lastTo} – last move squares; may be {@code null} before any move.</li>
 * </ul>
 *
 * <h2>Example</h2>
 * <pre>{@code
 * {
 *   "gameId": "abc123",
 *   "rev": 3,
 *   "position": { "e4": "wP", "e5": "bP", "e1": "wK", "e8": "bK" },
 *   "turn": "BLACK",
 *   "status": "IN_PROGRESS",
 *   "lastFrom": "e2",
 *   "lastTo": "e4"
 * }
 * }</pre>
 *
 * @param gameId   unique identifier of the game
 * @param rev      revision number (increments after each accepted move)
 * @param position board view map; keys "a1"..."h8", values are piece codes (e.g., "wK", "bQ")
 * @param turn     side to move; either {@code "WHITE"} or {@code "BLACK"}
 * @param status   status label for the game lifecycle
 * @param lastFrom last move origin square (nullable)
 * @param lastTo   last move destination square (nullable)
 * @author Alain Uwishema
 * @since 0.1
 */
public record GameStateDto(
        String gameId,
        int rev,
        Map<String, String> position,
        String turn,
        String status,
        String lastFrom,
        String lastTo
) {}
