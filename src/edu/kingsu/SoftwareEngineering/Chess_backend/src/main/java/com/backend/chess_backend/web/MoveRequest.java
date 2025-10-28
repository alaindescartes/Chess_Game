package com.backend.chess_backend.web;

/**
 * DTO representing a move request sent from the client to the backend.
 * <p>
 * This record carries the minimal information required for the backend to process a move
 * in an in-memory game instance. It is consumed by the {@code GameController#makeMove(String, MoveRequest)} endpoint.
 * </p>
 *
 * <h2>Fields</h2>
 * <ul>
 *   <li>{@code from} – origin square in algebraic notation (e.g., "e2").</li>
 *   <li>{@code to} – destination square in algebraic notation (e.g., "e4").</li>
 *   <li>{@code promotion} – optional promotion piece indicator ({@code "Q"}, {@code "R"}, {@code "B"}, or {@code "N"}); may be {@code null} if not applicable.</li>
 *   <li>{@code clientRev} – client's last known revision of the board. Used for conflict detection or optimistic concurrency.</li>
 * </ul>
 *
 * <h2>Example JSON</h2>
 * <pre>{@code
 * {
 *   "from": "e2",
 *   "to": "e4",
 *   "promotion": null,
 *   "clientRev": 0
 * }
 * }</pre>
 *
 * <p>
 * The backend uses {@code from} and {@code to} to identify source and destination squares.
 * Promotion, if provided, is case-insensitive and optional.
 * </p>
 *
 * @param from       the source square (algebraic, e.g., "e2")
 * @param to         the destination square (algebraic, e.g., "e4")
 * @param promotion  optional promotion piece (e.g., "Q" for queen); may be null
 * @param clientRev  the client's known board revision for concurrency checks
 * @author Alain Uwishema
 * @since 0.1
 */
public record MoveRequest(String from, String to, String promotion, Integer clientRev) { }
