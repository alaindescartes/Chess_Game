package com.backend.chess_backend;

import com.backend.chess_backend.domain.Board;
import com.backend.chess_backend.domain.BoardSetups;
import com.backend.chess_backend.domain.Piece;
import com.backend.chess_backend.domain.PieceColor;
import com.backend.chess_backend.domain.PieceType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link BoardSetups} standard chess initialization.
 */
public class BoardSetupsTest {

    private static void assertSquare(Board b, String sq, PieceColor color, PieceType type) {
        int idx = Board.sq(sq);
        Piece p = b.getAt(idx);
        assertNotNull(p, "Expected a piece on " + sq);
        assertEquals(color, p.getColor(), "Wrong color on " + sq);
        assertEquals(type, p.getType(), "Wrong type on " + sq);
        assertEquals(idx, p.getPosition(), "Piece position not synced for " + sq);
    }

    @Test
    @DisplayName("fillStandard: pawns on rank 2 (white) and rank 7 (black)")
    void pawns_arePlaced() {
        Board b = new Board();
        BoardSetups.fillStandard(b);

        for (int f = 0; f < 8; f++) {
            char file = (char) ('a' + f);
            assertSquare(b, "" + file + "2", PieceColor.WHITE, PieceType.PAWN);
            assertSquare(b, "" + file + "7", PieceColor.BLACK, PieceType.PAWN);
        }
    }

    @Test
    @DisplayName("fillStandard: back ranks have R,N,B,Q,K,B,N,R for each side")
    void backRanks_arePlacedInOrder() {
        Board b = new Board();
        BoardSetups.fillStandard(b);

        PieceType[] order = {
                PieceType.ROOK, PieceType.KNIGHT, PieceType.BISHOP,
                PieceType.QUEEN, PieceType.KING, PieceType.BISHOP,
                PieceType.KNIGHT, PieceType.ROOK
        };

        for (int f = 0; f < 8; f++) {
            char file = (char) ('a' + f);
            assertSquare(b, "" + file + "1", PieceColor.WHITE, order[f]);
            assertSquare(b, "" + file + "8", PieceColor.BLACK, order[f]);
        }
    }

    @Test
    @DisplayName("fillStandard: exactly 32 pieces, typical empty center squares are empty")
    void counts_andEmpties() {
        Board b = new Board();
        BoardSetups.fillStandard(b);

        // Count pieces
        int count = 0;
        for (int i = 0; i < 64; i++) {
            if (b.getAt(i) != null) count++;
        }
        assertEquals(32, count, "There should be exactly 32 pieces on the board");

        // A few central squares that should be empty in the start position
        assertNull(b.getAt(Board.sq("e3")), "e3 should be empty at start");
        assertNull(b.getAt(Board.sq("d4")), "d4 should be empty at start");
        assertNull(b.getAt(Board.sq("e5")), "e5 should be empty at start");
        assertNull(b.getAt(Board.sq("d6")), "d6 should be empty at start");
    }

    @Test
    @DisplayName("fillStandard: key piece sanity (kings and queens on correct squares)")
    void keyPieces_sanity() {
        Board b = new Board();
        BoardSetups.fillStandard(b);

        // White: queen on d1, king on e1
        assertSquare(b, "d1", PieceColor.WHITE, PieceType.QUEEN);
        assertSquare(b, "e1", PieceColor.WHITE, PieceType.KING);

        // Black: queen on d8, king on e8
        assertSquare(b, "d8", PieceColor.BLACK, PieceType.QUEEN);
        assertSquare(b, "e8", PieceColor.BLACK, PieceType.KING);
    }
}