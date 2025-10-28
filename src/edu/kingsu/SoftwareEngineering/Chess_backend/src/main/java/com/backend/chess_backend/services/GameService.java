package com.backend.chess_backend.services;

import com.backend.chess_backend.domain.Board;
import com.backend.chess_backend.domain.BoardSetups;
import com.backend.chess_backend.domain.BoardViews;
import com.backend.chess_backend.domain.PieceColor;
import com.backend.chess_backend.web.GameStateDto;
import com.backend.chess_backend.web.MoveRequest;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

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

        // Minimal: parse squares, move (no legality checks yet)
        int from = Board.sq(req.from());
        int to   = Board.sq(req.to());
        g.board.move(from, to);

        g.rev++;
        g.lastFrom = req.from();
        g.lastTo   = req.to();
        g.turn = (g.turn == PieceColor.WHITE) ? PieceColor.BLACK : PieceColor.WHITE;

        return g.toDto();
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
