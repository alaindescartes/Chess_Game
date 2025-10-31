package com.backend.chess_backend.services;

import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.backend.chess_backend.exception.IllegalActivity;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.backend.chess_backend.domain.Board;
import com.backend.chess_backend.domain.BoardSetups;
import com.backend.chess_backend.domain.BoardViews;
import com.backend.chess_backend.domain.PieceColor;
import com.backend.chess_backend.web.GameStateDto;
import com.backend.chess_backend.web.MoveRequest;

@Service
/**
 * Application service that manages in-memory chess games.
 * <p>
 * Exposes operations to create a game, fetch its current state, and apply a move.
 * State is held in a thread-safe map keyed by a server-generated game id. This service
 * does not enforce chess move legality; it performs naive moves and returns an
 * authoritative view via {@link com.backend.chess_backend.web.GameStateDto}.
 * </p>
 *
 * <h2>Concurrency</h2>
 * <p>
 * Backed by a {@link java.util.concurrent.ConcurrentMap}. Methods operate on a
 * per-game basis. If optimistic concurrency is desired, the API can compare
 * client-provided revisions with the server's {@code rev} and reject conflicts.
 * </p>
 *
 * @author Alain Uwishema
 * @since 0.1
 */
public class GameService {
    private final ConcurrentMap<String, Game> games = new ConcurrentHashMap<>();

    /**
     * Creates a new game with the standard chess starting position.
     *
     * @return the initial authoritative game state (revision 0, WHITE to move)
     */
    public GameStateDto createGame() {
        String id = UUID.randomUUID().toString();
        Board board = new Board();
        BoardSetups.fillStandard(board);
        Game g = new Game(id, board);
        games.put(id, g);
        return g.toDto();
    }

    /**
     * Returns the current authoritative state of a game.
     *
     * @param id the server-assigned game identifier
     * @return the game state DTO for the given id
     * @throws java.util.NoSuchElementException if no game exists for {@code id}
     */
    public GameStateDto getGame(String id) {
        Game g = games.get(id);
        if (g == null) throw new NoSuchElementException("Game not found: " + id);
        return g.toDto();
    }

    /**
     * Applies a naive move to the identified game. This method does not validate
     * chess rules (checks, pins, legal destinations, etc.). It parses algebraic
     * coordinates from the request, updates the board, bumps the revision, toggles
     * the side to move, and records the last move.
     *
     * @param id  the game identifier
     * @param req the move request containing {@code from}, {@code to}, and optional promotion/clientRev
     * @return the updated authoritative game state after the move
     * @throws java.util.NoSuchElementException if no game exists for {@code id}
     * @throws IllegalStateException if there is no piece on the {@code from} square
     * @throws IllegalArgumentException if either square decodes outside [0..63]
     */
    public GameStateDto makeMove(String id, MoveRequest req) {
        Game g = games.get(id);
        if (g == null) throw new NoSuchElementException("Game not found: " + id);
        validateBasicMove(g, req);

        int from = Board.sq(req.from());
        int to   = Board.sq(req.to());
        g.board.move(from, to);

        g.rev++;
        g.lastFrom = req.from();
        g.lastTo   = req.to();
        g.turn = (g.turn == PieceColor.WHITE) ? PieceColor.BLACK : PieceColor.WHITE;

        return g.toDto();
    }

    /**
     * Perform lightweight, pre-mutation validation for a proposed move.
     * <p>
     * Checks the following in order:
     * <ul>
     *   <li><b>Optimistic concurrency</b>: {@code req.clientRev == g.rev}; otherwise 409 CONFLICT.</li>
     *   <li><b>Square format</b>: {@code from}/{@code to} are algebraic squares in {@code a1..h8} (lowercase); otherwise 422 UNPROCESSABLE_ENTITY.</li>
     *   <li><b>Non-no-op</b>: {@code from} and {@code to} must differ; otherwise 422 UNPROCESSABLE_ENTITY.</li>
     *   <li><b>Presence & turn</b>: a piece exists on {@code from} and its color matches {@code g.turn}; otherwise 422 UNPROCESSABLE_ENTITY.</li>
     *   <li><b>Destination occupancy</b>: {@code to} must not contain a friendly piece; otherwise 422 UNPROCESSABLE_ENTITY.</li>
     * </ul>
     * This method does <em>not</em> enforce full chess legality (piece movement, path blocking,
     * check/castling/en passant/promotion); that belongs in the rules engine.
     * </p>
     *
     * @param g   current game aggregate (authoritative state)
     * @param req inbound move request (from/to/promotion/clientRev)
     * @throws org.springframework.web.server.ResponseStatusException
     *         if any validation fails (409 for stale revision; 422 for illegal inputs/conditions)
     */
    private void validateBasicMove(Game g, MoveRequest req) {
    
        if (req.clientRev() == null || !req.clientRev().equals(g.rev)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Client revision is stale; refresh and retry.");
        }

        // Squares
        if (!isSquare(req.from()) || !isSquare(req.to())) {
            throw new IllegalActivity( "Squares must be in a1..h8.");
        }
        if (req.from().equals(req.to())) {
            throw new IllegalActivity( "Destination cannot equal source.");
        }

        // Pieces & turn
        String fromCode = BoardViews.toPositionMap(g.board).get(req.from());
        if (fromCode == null) {
            throw new IllegalActivity(  "No piece on source square.");
        }
        char side = (g.turn == PieceColor.WHITE) ? 'w' : 'b';
        if (fromCode.charAt(0) != side) {
            throw new IllegalActivity( "It's not your turn to move that piece.");
        }

        // Friendly-occupied destination
        String toCode = BoardViews.toPositionMap(g.board).get(req.to());
        if (toCode != null && toCode.charAt(0) == fromCode.charAt(0)) {
            throw new IllegalActivity( "Cannot move onto a friendly piece.");
        }

    }

    /**
     * Determine whether the given string is a valid algebraic square label.
     * <p>
     * Accepts only lowercase file letters {@code a..h} and rank digits {@code 1..8},
     * e.g., {@code "e4"}. Uppercase (e.g., {@code "E4"}) is considered invalid by this check.
     * </p>
     *
     * @param s candidate string
     * @return {@code true} if {@code s} is in the range {@code a1..h8}; otherwise {@code false}
     */
    private boolean isSquare(String s) {
        return s != null && s.length() == 2 &&
                s.charAt(0) >= 'a' && s.charAt(0) <= 'h' &&
                s.charAt(1) >= '1' && s.charAt(1) <= '8';
    }

    /* ---- tiny in-memory Game aggregate ---- */
    /**
     * Internal aggregate representing a single in-memory game instance.
     * Holds the board, revision, side to move, last move markers, and status label.
     */
    private static final class Game {
        final String id;
        final Board board;
        int rev = 0;
        PieceColor turn = PieceColor.WHITE;
        String lastFrom, lastTo;
        String status = "IN_PROGRESS";

        Game(String id, Board board) { this.id = id; this.board = board; }

        /**
         * Projects the current aggregate state into a transport-friendly DTO
         * for API responses.
         *
         * @return a {@link com.backend.chess_backend.web.GameStateDto} snapshot of this game
         */
        GameStateDto toDto() {
            return new GameStateDto(
                    id,
                    rev,
                    BoardViews.toPositionMap(board),
                    turn.name(),
                    status,
                    lastFrom,
                    lastTo
            );
        }
    }
}
