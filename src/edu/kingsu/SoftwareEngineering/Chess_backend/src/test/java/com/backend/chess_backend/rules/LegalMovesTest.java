package com.backend.chess_backend.rules;

import com.backend.chess_backend.domain.Board;
import com.backend.chess_backend.domain.Piece;
import com.backend.chess_backend.domain.PieceColor;
import com.backend.chess_backend.domain.PieceType;
import com.backend.chess_backend.domain.rules.LegalMoves;
import com.backend.chess_backend.web.MoveRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link LegalMoves} covering pseudo-legal target generation
 * and the basic {@link LegalMoves#isLegal(Board, PieceColor, MoveRequest)} gate.
 */
public class LegalMovesTest {

    private static Piece W(PieceType t, String sq) {
        int i = Board.sq(sq);
        return new Piece(PieceColor.WHITE, t, i);
    }
    private static Piece B(PieceType t, String sq) {
        int i = Board.sq(sq);
        return new Piece(PieceColor.BLACK, t, i);
    }

    @Test
    @DisplayName("Pawn: initial one/two push, blocking, and diagonal captures")
    void pawnTargets_basic() {
        LegalMoves lm = new LegalMoves();
        Board b = new Board();

        // White pawn on e2, clear path → e3 & e4
        b.setAt(Board.sq("e2"), W(PieceType.PAWN, "e2"));
        List<String> t1 = lm.pseudoLegalTargets(b, "e2");
        assertTrue(t1.contains("e3"), "e3 should be available from e2");
        assertTrue(t1.contains("e4"), "e4 (double push) should be available from e2");

        // Block e3 → no forward moves (including the double)
        b.setAt(Board.sq("e3"), W(PieceType.PAWN, "e3"));
        List<String> t2 = lm.pseudoLegalTargets(b, "e2");
        assertFalse(t2.contains("e3"));
        assertFalse(t2.contains("e4"));

        // Clear e3 and add black pieces on d3/f3 → diagonal captures allowed
        b.clear(Board.sq("e3"));
        b.setAt(Board.sq("d3"), B(PieceType.KNIGHT, "d3"));
        b.setAt(Board.sq("f3"), B(PieceType.BISHOP, "f3"));
        List<String> t3 = lm.pseudoLegalTargets(b, "e2");
        assertTrue(t3.contains("d3"));
        assertTrue(t3.contains("f3"));
    }

    @Test
    @DisplayName("Knight: L moves ignore blockers; friendly-occupied squares excluded")
    void knightTargets_basic() {
        LegalMoves lm = new LegalMoves();
        Board b = new Board();
        b.setAt(Board.sq("b1"), W(PieceType.KNIGHT, "b1"));

        // Friendly on c3 should exclude c3
        b.setAt(Board.sq("c3"), W(PieceType.PAWN, "c3"));

        List<String> t = lm.pseudoLegalTargets(b, "b1");
        assertTrue(t.contains("a3"));
        assertTrue(t.contains("d2"));
        assertTrue(t.contains("c3") == false, "friendly-occupied c3 must be excluded");
    }

    @Test
    @DisplayName("Rook: stops at first blocker; can capture enemy then stop")
    void rookTargets_blockingAndCapture() {
        LegalMoves lm = new LegalMoves();
        Board b = new Board();
        b.setAt(Board.sq("a1"), W(PieceType.ROOK, "a1"));

        // Friendly blocker on a2 → cannot go past a2 vertically
        b.setAt(Board.sq("a2"), W(PieceType.PAWN, "a2"));
        // Enemy on d1 → allowed up to and including d1, then stop
        b.setAt(Board.sq("d1"), B(PieceType.PAWN, "d1"));

        List<String> t = lm.pseudoLegalTargets(b, "a1");
        assertTrue(t.contains("b1"));
        assertTrue(t.contains("c1"));
        assertTrue(t.contains("d1"));
        assertFalse(t.contains("e1"), "must stop after first capture");
        assertFalse(t.contains("a2"), "friendly blocker directly above prevents vertical movement");
    }

    @Test
    @DisplayName("King: 8 neighbors (minus friendly-occupied)")
    void kingTargets_neighbors() {
        LegalMoves lm = new LegalMoves();
        Board b = new Board();
        b.setAt(Board.sq("e4"), W(PieceType.KING, "e4"));
        b.setAt(Board.sq("e5"), W(PieceType.PAWN, "e5")); // friendly blocks one
        b.setAt(Board.sq("f5"), B(PieceType.PAWN, "f5")); // enemy can be captured

        List<String> t = lm.pseudoLegalTargets(b, "e4");
        assertFalse(t.contains("e5"));
        assertTrue(t.contains("f5"));
        assertTrue(t.contains("d3"));
        assertTrue(t.contains("f3"));
    }

    @Test
    @DisplayName("pseudoLegalTargets: invalid or empty-from returns empty list")
    void invalidFrom() {
        LegalMoves lm = new LegalMoves();
        Board b = new Board();

        assertTrue(lm.pseudoLegalTargets(b, "e2").isEmpty(), "empty square → no targets");
        assertTrue(lm.pseudoLegalTargets(b, "z9").isEmpty(), "invalid algebraic → empty result");
    }

    @Test
    @DisplayName("isLegal: uses pattern + occupancy; rejects illegal shapes and friendly destinations")
    void isLegal_basicGate() {
        LegalMoves lm = new LegalMoves();
        Board b = new Board();

        // Rook a1 along rank to capture on d1
        b.setAt(Board.sq("a1"), W(PieceType.ROOK, "a1"));
        b.setAt(Board.sq("d1"), B(PieceType.BISHOP, "d1"));
        MoveRequest ok = new MoveRequest("a1", "d1", null, 0);
        assertTrue(lm.isLegal(b, PieceColor.WHITE, ok));

        // Illegal diagonal for rook
        MoveRequest badShape = new MoveRequest("a1", "b2", null, 0);
        assertFalse(lm.isLegal(b, PieceColor.WHITE, badShape));

        // Friendly destination is illegal
        b.setAt(Board.sq("b1"), W(PieceType.KNIGHT, "b1"));
        MoveRequest toFriend = new MoveRequest("a1", "b1", null, 0);
        assertFalse(lm.isLegal(b, PieceColor.WHITE, toFriend));
    }
}
