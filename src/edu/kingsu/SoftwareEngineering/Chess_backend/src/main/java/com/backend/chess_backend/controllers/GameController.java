package com.backend.chess_backend.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;

import com.backend.chess_backend.services.GameService;
import com.backend.chess_backend.web.GameStateDto;
import com.backend.chess_backend.web.MoveRequest;

/**
 * REST controller exposing endpoints for managing in-memory chess games.
 * <p>
 * Delegates core logic to {@link com.backend.chess_backend.services.GameService} and provides
 * three primary routes:
 * </p>
 * <ul>
 *   <li><b>POST /api/game</b> — create a new game with the standard chess setup.</li>
 *   <li><b>GET /api/game/{id}</b> — fetch the current state of a specific game.</li>
 *   <li><b>POST /api/game/{id}/move</b> — submit a move request to update game state.</li>
 * </ul>
 *
 * <p>
 * Each endpoint returns a {@link com.backend.chess_backend.web.GameStateDto} describing the full
 * board view, revision number, and game status. The controller does not perform move validation;
 * rule enforcement is handled by the service layer.
 * </p>
 *
 * @author Alain Uwishema
 * @since 0.1
 */
@RestController
@RequestMapping("/api/game")
public class GameController {

    private final GameService service;

    /**
     * Constructs a new {@code GameController} with the given service dependency.
     *
     * @param service the game service handling core logic
     */
    public GameController(GameService service) {
        this.service = service;
    }

    /**
     * Creates a new chess game with a standard initial setup.
     * <p>
     * Returns a fully populated {@link com.backend.chess_backend.web.GameStateDto} containing all
     * pieces in their starting positions, revision 0, and WHITE as the side to move.
     * </p>
     *
     * @return the newly created game state
     */
    @PostMapping
    public GameStateDto createGame() {
        return service.createGame();
    }

    /**
     * Retrieves the current state of an existing game.
     *
     * @param id the game identifier (UUID string)
     * @return the game state DTO corresponding to {@code id}
     * @throws java.util.NoSuchElementException if the game ID does not exist
     */
    @GetMapping("/{id}")
    public GameStateDto getGame(@PathVariable String id) {
        return service.getGame(id);
    }

    /**
     * Applies a move request to a specific game.
     * <p>
     * Delegates to {@link com.backend.chess_backend.services.GameService#makeMove(String, com.backend.chess_backend.web.MoveRequest)}.
     * This endpoint does not enforce rule legality but updates the in-memory board state accordingly.
     * </p>
     *
     * @param id  the game identifier
     * @param req the move request including from/to squares and optional promotion
     * @return the updated game state after applying the move
     * @throws java.util.NoSuchElementException if no game exists for {@code id}
     * @throws IllegalArgumentException if provided coordinates are invalid
     */
    @PostMapping("/{id}/move")
    public GameStateDto makeMove(@PathVariable String id, @RequestBody MoveRequest req) {
        return service.makeMove(id, req);
    }

    /**
     * Returns pseudo-legal destination squares for the piece on {@code from}.
     * <p>
     * This is intended for frontend highlighting. It returns movement-pattern targets with
     * board bounds and occupancy rules applied (captures allowed), but may not include
     * king-safety constraints yet.
     * </p>
     *
     * Example: <code>GET /api/game/{id}/targets?from=e2</code> → <code>["e3","e4"]</code>
     *
     * @param id   game identifier (UUID string)
     * @param from source square in algebraic notation ("a1".."h8")
     * @return list of algebraic squares that the selected piece can move to
     */
    @GetMapping("/{id}/targets")
    public List<String> getTargets(@PathVariable String id, @RequestParam("from") String from) {
        return service.getPseudoLegalTargets(id, from);
    }
}
