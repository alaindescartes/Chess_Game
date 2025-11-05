package com.backend.chess_backend;

import com.backend.chess_backend.domain.Board;
import com.backend.chess_backend.domain.Piece;
import com.backend.chess_backend.domain.PieceColor;
import com.backend.chess_backend.domain.PieceType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link Board} covering indexing helpers and basic mutation APIs.
 */
public class BoardTest {

    private static Piece W(PieceType t, String sq) {
        return new Piece(PieceColor.WHITE, t, Board.sq(sq));
    }
    private static Piece B(PieceType t, String sq) {
        return new Piece(PieceColor.BLACK, t, Board.sq(sq));
    }

    @Test
    @DisplayName("sq(): algebraic → index mapping and validation")
    void sq_algebraicToIndex() {
        assertEquals(0, Board.sq("a1"));
        assertEquals(7, Board.sq("h1"));
        assertEquals(56, Board.sq("a8"));
        assertEquals(28, Board.sq("e4")); // (rank=3 << 3) | file=4

        assertThrows(IllegalArgumentException.class, () -> Board.sq(null));
        assertThrows(IllegalArgumentException.class, () -> Board.sq(""));
        assertThrows(IllegalArgumentException.class, () -> Board.sq("e9"));
        assertThrows(IllegalArgumentException.class, () -> Board.sq("i1"));
        assertThrows(IllegalArgumentException.class, () -> Board.sq("aa"));
    }

    @Test
    @DisplayName("toAlgebraic(): index → algebraic mapping and validation")
    void toAlgebraic_indexToAlgebraic() {
        assertEquals("a1", Board.toAlgebraic(0));
        assertEquals("h1", Board.toAlgebraic(7));
        assertEquals("a8", Board.toAlgebraic(56));
        assertEquals("e4", Board.toAlgebraic(28));

        assertThrows(IllegalArgumentException.class, () -> Board.toAlgebraic(-1));
        assertThrows(IllegalArgumentException.class, () -> Board.toAlgebraic(64));
    }

    @Test
    @DisplayName("setAt/getAt synchronize piece position and board occupancy")
    void setGet_roundTrip() {
        Board b = new Board();
        int e4 = Board.sq("e4");
        Piece rook = W(PieceType.ROOK, "a1"); // constructed at a1

        b.setAt(e4, rook);
        assertSame(rook, b.getAt(e4));
        assertNull(b.getAt(Board.sq("a1"))); // not placed there
        assertEquals(e4, rook.getPosition(), "Piece.moveTo(square) should sync internal position");
    }

    @Test
    @DisplayName("clear(): removes and returns piece; leaves square empty")
    void clear_removesPiece() {
        Board b = new Board();
        int d5 = Board.sq("d5");
        Piece knight = W(PieceType.KNIGHT, "d5");
        b.setAt(d5, knight);

        Piece removed = b.clear(d5);
        assertSame(knight, removed);
        assertNull(b.getAt(d5));
    }

    @Test
    @DisplayName("move(): relocates piece, updates its position, captures if destination occupied")
    void move_basicAndCapture() {
        Board b = new Board();
        int a1 = Board.sq("a1");
        int d1 = Board.sq("d1");

        Piece rook = W(PieceType.ROOK, "a1");
        b.setAt(a1, rook);

        // Enemy on d1 to capture
        Piece enemy = B(PieceType.BISHOP, "d1");
        b.setAt(d1, enemy);

        b.move(a1, d1);
        assertSame(rook, b.getAt(d1));
        assertNull(b.getAt(a1));
        assertEquals(d1, rook.getPosition());
    }

    @Test
    @DisplayName("move(): throws when no piece on from-square")
    void move_throwsWhenEmptyFrom() {
        Board b = new Board();
        int from = Board.sq("a1");
        int to = Board.sq("a2");
        assertThrows(IllegalStateException.class, () -> b.move(from, to));
    }

    @Test
    @DisplayName("validateSquare(): enforces [0..63] range")
    void validateSquare_range() {
        assertDoesNotThrow(() -> Board.validateSquare(0));
        assertDoesNotThrow(() -> Board.validateSquare(63));
        assertThrows(IllegalArgumentException.class, () -> Board.validateSquare(-1));
        assertThrows(IllegalArgumentException.class, () -> Board.validateSquare(64));
    }
}