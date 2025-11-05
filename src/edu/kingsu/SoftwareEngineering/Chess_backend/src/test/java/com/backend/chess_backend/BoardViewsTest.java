package com.backend.chess_backend;

import com.backend.chess_backend.domain.Board;
import com.backend.chess_backend.domain.BoardSetups;
import com.backend.chess_backend.domain.BoardViews;
import com.backend.chess_backend.domain.Piece;
import com.backend.chess_backend.domain.PieceColor;
import com.backend.chess_backend.domain.PieceType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link BoardViews} presentation mapping.
 */
public class BoardViewsTest {

    private static Piece W(PieceType t, String sq) {
        return new Piece(PieceColor.WHITE, t, Board.sq(sq));
    }
    private static Piece B(PieceType t, String sq) {
        return new Piece(PieceColor.BLACK, t, Board.sq(sq));
    }

    @Test
    @DisplayName("toPositionMap: empty board -> empty map")
    void emptyBoard_returnsEmptyMap() {
        Board b = new Board();
        Map<String, String> view = BoardViews.toPositionMap(b);
        assertNotNull(view);
        assertTrue(view.isEmpty());
    }

    @Test
    @DisplayName("toPositionMap: standard setup contains 32 pieces with correct codes")
    void standardSetup_hasExpectedEntries() {
        Board b = new Board();
        BoardSetups.fillStandard(b);

        Map<String, String> view = BoardViews.toPositionMap(b);
        assertEquals(32, view.size(), "Standard chess start should have 32 occupied squares");

        // A few spot checks for correctness
        assertEquals("wR", view.get("a1"));
        assertEquals("wN", view.get("b1"));
        assertEquals("wB", view.get("c1"));
        assertEquals("wQ", view.get("d1"));
        assertEquals("wK", view.get("e1"));
        assertEquals("wB", view.get("f1"));
        assertEquals("wN", view.get("g1"));
        assertEquals("wR", view.get("h1"));

        assertEquals("wP", view.get("a2"));
        assertEquals("wP", view.get("h2"));

        assertEquals("bP", view.get("a7"));
        assertEquals("bP", view.get("h7"));

        assertEquals("bR", view.get("a8"));
        assertEquals("bN", view.get("b8"));
        assertEquals("bB", view.get("c8"));
        assertEquals("bQ", view.get("d8"));
        assertEquals("bK", view.get("e8"));
        assertEquals("bB", view.get("f8"));
        assertEquals("bN", view.get("g8"));
        assertEquals("bR", view.get("h8"));

        // Empty center squares not present
        assertFalse(view.containsKey("e3"));
        assertFalse(view.containsKey("d4"));
    }

    @Test
    @DisplayName("toPositionMap: iteration order is deterministic (ranks 1,2 then 7,8)")
    void deterministicOrder_linkedHashMap() {
        Board b = new Board();
        BoardSetups.fillStandard(b);

        Map<String, String> view = BoardViews.toPositionMap(b);
        List<String> keys = new ArrayList<>(view.keySet());

        // First 16 entries should be a1..h1, a2..h2
        List<String> expectedHead = new ArrayList<>();
        for (char f = 'a'; f <= 'h'; f++) expectedHead.add(f + "1");
        for (char f = 'a'; f <= 'h'; f++) expectedHead.add(f + "2");

        assertEquals(expectedHead, keys.subList(0, 16));

        // Last 16 entries should be a7..h7, a8..h8
        List<String> expectedTail = new ArrayList<>();
        for (char f = 'a'; f <= 'h'; f++) expectedTail.add(f + "7");
        for (char f = 'a'; f <= 'h'; f++) expectedTail.add(f + "8");

        List<String> actualTail = keys.subList(keys.size() - 16, keys.size());
        assertEquals(expectedTail, actualTail);
    }

    @Test
    @DisplayName("toPositionMap: codes reflect piece color and type (wK, wQ, wR, wB, wN, wP / bK..)")
    void codes_coverAllPieceTypes() {
        Board b = new Board();

        // Place one of each white and black type on unique squares
        b.setAt(Board.sq("a1"), W(PieceType.KING, "a1"));
        b.setAt(Board.sq("b1"), W(PieceType.QUEEN, "b1"));
        b.setAt(Board.sq("c1"), W(PieceType.ROOK, "c1"));
        b.setAt(Board.sq("d1"), W(PieceType.BISHOP, "d1"));
        b.setAt(Board.sq("e1"), W(PieceType.KNIGHT, "e1"));
        b.setAt(Board.sq("f1"), W(PieceType.PAWN, "f1"));

        b.setAt(Board.sq("a8"), B(PieceType.KING, "a8"));
        b.setAt(Board.sq("b8"), B(PieceType.QUEEN, "b8"));
        b.setAt(Board.sq("c8"), B(PieceType.ROOK, "c8"));
        b.setAt(Board.sq("d8"), B(PieceType.BISHOP, "d8"));
        b.setAt(Board.sq("e8"), B(PieceType.KNIGHT, "e8"));
        b.setAt(Board.sq("f8"), B(PieceType.PAWN, "f8"));

        Map<String, String> view = BoardViews.toPositionMap(b);

        assertEquals("wK", view.get("a1"));
        assertEquals("wQ", view.get("b1"));
        assertEquals("wR", view.get("c1"));
        assertEquals("wB", view.get("d1"));
        assertEquals("wN", view.get("e1"));
        assertEquals("wP", view.get("f1"));

        assertEquals("bK", view.get("a8"));
        assertEquals("bQ", view.get("b8"));
        assertEquals("bR", view.get("c8"));
        assertEquals("bB", view.get("d8"));
        assertEquals("bN", view.get("e8"));
        assertEquals("bP", view.get("f8"));
    }

    @Test
    @DisplayName("toPositionMap: null board throws NullPointerException")
    void nullBoard_throws() {
        assertThrows(NullPointerException.class, () -> BoardViews.toPositionMap(null));
    }
}