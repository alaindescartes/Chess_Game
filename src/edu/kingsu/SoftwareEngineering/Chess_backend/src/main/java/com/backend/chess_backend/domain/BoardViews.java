package com.backend.chess_backend.domain;


import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Presentation utilities for converting a {@link Board} into data structures suitable
 * for front-end rendering or external inspection.
 * <p>
 * Provides static helpers that translate internal 0..63 indexed board data into
 * a human-readable or JSON-friendly map keyed by algebraic coordinates (e.g., "e2" → "wP").
 * </p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *   <li>Expose internal board state without leaking mutable references.</li>
 *   <li>Provide a compact string encoding for pieces: prefix with color (w/b), followed by piece letter (K, Q, R, B, N, P).</li>
 *   <li>Ensure deterministic iteration order using {@link java.util.LinkedHashMap}.</li>
 * </ul>
 *
 * <h2>Example</h2>
 * <pre>
 * Board b = new Board();
 * BoardSetups.fillStandard(b);
 * Map<String, String> view = BoardViews.toPositionMap(b);
 * // view.get("e2") == "wP"
 * // view.get("e8") == "bK"
 * </pre>
 *
 * @author Alain Uwishema
 * @since 0.1
 */

public class BoardViews {
    private BoardViews() {}

    /**
     * Converts the internal {@link Board} representation to a map from algebraic square
     * coordinates (e.g., "e2") to two-character piece codes (e.g., "wP" or "bK").
     * <p>
     * Used primarily for serialization to clients that expect lightweight board views.
     * </p>
     *
     * @param b the board to convert; must not be {@code null}
     * @return a {@link java.util.LinkedHashMap} with entries for all occupied squares, preserving natural order
     * @since 0.1
     */
    public static Map<String,String> toPositionMap(Board b) {
        Map<String,String> map = new LinkedHashMap<>(64);
        for (int sq = 0; sq < 64; sq++) {
            Piece p = b.getAt(sq);
            if (p == null) continue;
            map.put(Board.toAlgebraic(sq), toCode(p));
        }
        return map;
    }

    /**
     * Produces a short code representing a {@link Piece}.
     * The format is: {@code wK, wQ, wR, wB, wN, wP} for white pieces and
     * {@code bK, bQ, bR, bB, bN, bP} for black.
     *
     * @param p the piece to encode; must not be {@code null}
     * @return a compact two-character identifier for the piece
     * @since 0.1
     */
    private static String toCode(Piece p) {
        char side = (p.getColor() == PieceColor.WHITE) ? 'w' : 'b';
        char t;
        switch (p.getType()) {
            case KING   -> t = 'K';
            case QUEEN  -> t = 'Q';
            case ROOK   -> t = 'R';
            case BISHOP -> t = 'B';
            case KNIGHT -> t = 'N';
            case PAWN   -> t = 'P';
            default     -> t = '?';
        }
        return "" + side + t;
    }
}
