
package com.backend.chess_backend.domain;

/**
 * Utilities for initializing {@link Board} positions.
 * <p>
 * Currently provides a single helper to populate the standard chess starting layout:
 * white pieces on ranks 1–2, black pieces on ranks 7–8. Squares are addressed via
 * algebraic coordinates (e.g., "e2"), converted to 0..63 indices by {@link Board#sq(String)}.
 * </p>
 *
 * <h2>Standard layout</h2>
 * <pre>
 * White back rank: a1 rook, b1 knight, c1 bishop, d1 queen, e1 king, f1 bishop, g1 knight, h1 rook
 * White pawns: a2...h2
 * Black pawns: a7...h7
 * Black back rank: a8 rook, b8 knight, c8 bishop, d8 queen, e8 king, f8 bishop, g8 knight, h8 rook
 * </pre>
 *
 * <p>
 * This class is not instantiable.
 * </p>
 *
 * @author Alain Uwishema
 * @since 0.1
 */
public class BoardSetups {
    private BoardSetups() {}

    /**
     * Populates the given board with the standard chess starting position.
     * <p>
     * Pawns are placed on ranks 2 (white) and 7 (black). Back ranks are filled using the
     * canonical piece order: rook, knight, bishop, queen, king, bishop, knight, rook.
     * The provided {@link Board} is modified in place via {@link Board#setAt(int, Piece)}.
     * </p>
     *
     * @param b the board to fill; must not be {@code null}
     * @since 0.1
     */
    public static void fillStandard(Board b) {
        // Pawns
        for (int f = 0; f < 8; f++) {
            int wSq = Board.sq("" + (char)('a' + f) + "2");
            int bSq = Board.sq("" + (char)('a' + f) + "7");
            b.setAt(wSq, new Piece(PieceColor.WHITE, PieceType.PAWN, wSq));
            b.setAt(bSq, new Piece(PieceColor.BLACK, PieceType.PAWN, bSq));
        }
        // Back ranks (file order: R, N, B, Q, K, B, N, R)
        PieceType[] order = { PieceType.ROOK, PieceType.KNIGHT, PieceType.BISHOP,
                PieceType.QUEEN, PieceType.KING, PieceType.BISHOP,
                PieceType.KNIGHT, PieceType.ROOK };
        for (int f = 0; f < 8; f++) {
            int iW = Board.sq("" + (char)('a' + f) + "1");
            int iB = Board.sq("" + (char)('a' + f) + "8");
            b.setAt(iW, new Piece(PieceColor.WHITE, order[f], iW));
            b.setAt(iB, new Piece(PieceColor.BLACK, order[f], iB));
        }
    }
}
