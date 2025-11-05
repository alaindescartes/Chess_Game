package com.backend.chess_backend;

import com.backend.chess_backend.domain.Board;
import com.backend.chess_backend.domain.Piece;
import com.backend.chess_backend.domain.PieceColor;
import com.backend.chess_backend.domain.PieceType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link Piece}.
 */
public class PieceTest {

    @Test
    @DisplayName("Constructor sets color/type/position and defaults hasMoved=false")
    void constructor_initialState() {
        int b1 = Board.sq("b1");
        Piece p = new Piece(PieceColor.WHITE, PieceType.KNIGHT, b1);

        assertEquals(PieceColor.WHITE, p.getColor());
        assertEquals(PieceType.KNIGHT, p.getType());
        assertEquals(b1, p.getPosition());
        assertFalse(p.hasMoved(), "Newly constructed piece should have hasMoved=false");
    }

    @Test
    @DisplayName("moveTo updates position without toggling hasMoved")
    void moveTo_updatesPosition_only() {
        int e2 = Board.sq("e2");
        int e4 = Board.sq("e4");
        Piece p = new Piece(PieceColor.WHITE, PieceType.PAWN, e2);

        // Initially not moved
        assertFalse(p.hasMoved());
        p.moveTo(e4);
        assertEquals(e4, p.getPosition());
        assertFalse(p.hasMoved(), "moveTo should NOT change hasMoved");

        // If already marked moved, moveTo should not reset it
        p.setHasMoved(true);
        p.moveTo(e2);
        assertEquals(e2, p.getPosition());
        assertTrue(p.hasMoved(), "moveTo should NOT change hasMoved when true");
    }

    @Test
    @DisplayName("setHasMoved toggles movement flag independently")
    void setHasMoved_independent() {
        int a1 = Board.sq("a1");
        Piece p = new Piece(PieceColor.BLACK, PieceType.ROOK, a1);

        assertFalse(p.hasMoved());
        p.setHasMoved(true);
        assertTrue(p.hasMoved());
        p.setHasMoved(false);
        assertFalse(p.hasMoved());
    }

    @Test
    @DisplayName("toString contains type, color, position, and hasMoved")
    void toString_containsKeyFields() {
        int d8 = Board.sq("d8");
        Piece p = new Piece(PieceColor.BLACK, PieceType.QUEEN, d8);
        p.setHasMoved(true);

        String s = p.toString();
        assertNotNull(s);
        assertTrue(s.contains("type=QUEEN"));
        assertTrue(s.contains("color=BLACK"));
        assertTrue(s.contains("position=" + d8));
        assertTrue(s.contains("hasMoved=true"));
    }
}