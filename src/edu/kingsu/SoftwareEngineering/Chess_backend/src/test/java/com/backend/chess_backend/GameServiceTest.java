package com.backend.chess_backend;

import com.backend.chess_backend.domain.rules.LegalMoves;
import com.backend.chess_backend.services.GameService;
import com.backend.chess_backend.web.GameStateDto;
import com.backend.chess_backend.web.MoveRequest;
import com.backend.chess_backend.exception.IllegalActivity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link GameService}.
 * Uses the real rules engine ({@link LegalMoves}) and standard start positions.
 */
public class GameServiceTest {

    private GameService newService() {
        return new GameService(new LegalMoves());
    }

    @Test
    @DisplayName("createGame: returns standard start, rev=0, WHITE to move, IN_PROGRESS")
    void createGame_initialState() {
        GameService svc = newService();

        GameStateDto dto = svc.createGame();
        assertNotNull(dto.gameId());
        assertEquals(0, dto.rev());
        assertEquals("WHITE", dto.turn());
        assertEquals("IN_PROGRESS", dto.status());
        assertNull(dto.lastFrom());
        assertNull(dto.lastTo());

        Map<String,String> pos = dto.position();
        assertNotNull(pos);
        assertEquals(32, pos.size(), "Standard chess start should have 32 occupied squares");

        // Spot-check a few canonical squares
        assertEquals("wR", pos.get("a1"));
        assertEquals("wN", pos.get("b1"));
        assertEquals("wP", pos.get("e2"));
        assertEquals("bP", pos.get("e7"));
        assertEquals("bK", pos.get("e8"));
    }

    @Test
    @DisplayName("getGame: returns existing game; throws for missing id")
    void getGame_basicAndMissing() {
        GameService svc = newService();
        String id = svc.createGame().gameId();

        GameStateDto fetched = svc.getGame(id);
        assertEquals(id, fetched.gameId());

        assertThrows(NoSuchElementException.class, () -> svc.getGame("does-not-exist"));
    }

    @Test
    @DisplayName("makeMove: legal pawn advance e2→e4 updates rev/turn/last move and board")
    void makeMove_legalPawnAdvance() {
        GameService svc = newService();
        GameStateDto start = svc.createGame();
        String id = start.gameId();

        GameStateDto after = svc.makeMove(id, new MoveRequest("e2", "e4", null, start.rev()));

        assertEquals(1, after.rev(), "rev should increment");
        assertEquals("BLACK", after.turn(), "turn should flip after a legal white move");
        assertEquals("e2", after.lastFrom());
        assertEquals("e4", after.lastTo());

        // Board view should reflect move
        assertFalse(after.position().containsKey("e2"), "e2 should now be empty");
        assertEquals("wP", after.position().get("e4"), "white pawn should be on e4");
    }

    @Test
    @DisplayName("makeMove: stale clientRev → 409 ResponseStatusException")
    void makeMove_staleRevision() {
        GameService svc = newService();
        GameStateDto start = svc.createGame();
        String id = start.gameId();

        // Wrong client revision (expect 409 via ResponseStatusException)
        assertThrows(ResponseStatusException.class,
                () -> svc.makeMove(id, new MoveRequest("e2", "e4", null, 999)));
    }

    @Test
    @DisplayName("makeMove: empty source square → IllegalActivity 'No piece on source square'")
    void makeMove_noPieceOnSource() {
        GameService svc = newService();
        GameStateDto start = svc.createGame();
        String id = start.gameId();

        // e3 is empty at start
        IllegalActivity ex = assertThrows(IllegalActivity.class,
                () -> svc.makeMove(id, new MoveRequest("e3", "e4", null, start.rev())));
        assertTrue(ex.getMessage().toLowerCase().contains("no piece"), "Expected 'No piece on source square' message");
    }

    @Test
    @DisplayName("makeMove: illegal shape (e2→e5 for pawn) → IllegalActivity from rules engine")
    void makeMove_illegalShape() {
        GameService svc = newService();
        GameStateDto start = svc.createGame();
        String id = start.gameId();

        // e2→e5 is not a legal pawn move from start
        IllegalActivity ex = assertThrows(IllegalActivity.class,
                () -> svc.makeMove(id, new MoveRequest("e2", "e5", null, start.rev())));
        assertTrue(ex.getMessage().toLowerCase().contains("illegal move"), "Expected 'Illegal move' from rules engine");
    }

    @Test
    @DisplayName("makeMove: invalid square labels → IllegalActivity from basic validation")
    void makeMove_invalidSquares() {
        GameService svc = newService();
        GameStateDto start = svc.createGame();
        String id = start.gameId();

        assertThrows(IllegalActivity.class,
                () -> svc.makeMove(id, new MoveRequest("z9", "e4", null, start.rev())));
        assertThrows(IllegalActivity.class,
                () -> svc.makeMove(id, new MoveRequest("e2", "i1", null, start.rev())));
        assertThrows(IllegalActivity.class,
                () -> svc.makeMove(id, new MoveRequest("e2", "e2", null, start.rev()))); // same from/to
    }

    @Test
    @DisplayName("getPseudoLegalTargets: returns pawn targets from e2 (e3,e4) in start position")
    void targets_pawnFromE2() {
        GameService svc = newService();
        GameStateDto start = svc.createGame();
        String id = start.gameId();

        List<String> targets = svc.getPseudoLegalTargets(id, "e2");
        assertNotNull(targets);
        assertTrue(targets.contains("e3"));
        assertTrue(targets.contains("e4"));
    }

    @Test
    @DisplayName("getPseudoLegalTargets: empty for black piece when it's WHITE to move")
    void targets_wrongTurn_empty() {
        GameService svc = newService();
        String id = svc.createGame().gameId();

        // It's WHITE to move at start; black pawn e7 should return no targets
        List<String> targets = svc.getPseudoLegalTargets(id, "e7");
        assertNotNull(targets);
        assertTrue(targets.isEmpty(), "Should be empty when selecting the side not to move");
    }
}