package com.backend.chess_backend.domain;

/**
 * Minimal, in-memory representation of a chess board.
 * <p>
 * The board stores up to 64 {@link Piece} references indexed from 0..63 where
 * a1 = 0, h1 = 7, a8 = 56, and h8 = 63. It provides only basic placement and
 * movement operations; rule legality (check, pins, castling, etc.) belongs in
 * higher-level services.
 * </p>
 *
 * <h2>Indexing</h2>
 * <pre>
 * index = (rank << 3) | file
 * file: 0..7 for a..h, rank: 0..7 for 1..8
 * Examples: a1=0, h1=7, a8=56, e4=28
 * </pre>
 *
 * @author Alain Uwishema
 * @since 0.1
 */
public final class Board {
   private final Piece[] squares ;

    /**
     * Creates an empty 8x8 board (all squares initially {@code null}).
     */
    public Board() {
        this.squares = new Piece[64];
    }
    /**
     * Returns the piece at a given square or {@code null} if the square is empty.
     *
     * @param square board index in [0..63]
     * @return the piece at {@code square}, or {@code null}
     * @throws IllegalArgumentException if {@code square} is outside [0..63]
     */
    public Piece getAt(int square) {
        validateSquare(square);
        return squares[square];
    }

    /**
     * Places (or replaces) a piece on a given square. If {@code piece} is non-null,
     * its internal position is synchronized to {@code square}.
     *
     * @param square board index in [0..63]
     * @param piece  the piece to place, or {@code null} to clear the square
     * @throws IllegalArgumentException if {@code square} is outside [0..63]
     */
    public void setAt(int square, Piece piece) {
        validateSquare(square);
        squares[square] = piece;
        if (piece != null) piece.moveTo(square);
    }

    /**
     * Removes and returns any piece at the given square.
     *
     * @param square board index in [0..63]
     * @return the removed piece, or {@code null} if the square was empty
     * @throws IllegalArgumentException if {@code square} is outside [0..63]
     */
    public Piece clear(int square) {
        validateSquare(square);
        Piece p = squares[square];
        squares[square] = null;
        return p;
    }

    /**
     * Naively moves a piece from one square to another (no legality checks).
     * If a piece exists at {@code to}, it is overwritten (captured). The moved
     * piece's internal position is synchronized to {@code to} and may update its
     * moved flag depending on {@link Piece#moveTo(int)} semantics.
     *
     * @param from source square index in [0..63]
     * @param to   destination square index in [0..63]
     * @throws IllegalArgumentException if either index is outside [0..63]
     * @throws IllegalStateException    if there is no piece on {@code from}
     */
    public void move(int from, int to) {
        validateSquare(from); validateSquare(to);
        Piece p = squares[from];
        if (p == null) throw new IllegalStateException("No piece on from-square " + from);
        squares[from] = null;
        squares[to] = p;
        p.moveTo(to);
    }

    /**
     * Converts an algebraic coordinate (e.g., {@code "e4"}) to a 0..63 index.
     *
     * @param algebraic two-character square like {@code "a1"}..{@code "h8"}
     * @return the index in [0..63]
     * @throws IllegalArgumentException if the input is null, not length 2, or decodes outside [0..63]
     */
    public static int sq(String algebraic) {
        if (algebraic == null || algebraic.length() != 2) {
            throw new IllegalArgumentException("Algebraic must be like \"e4\"");
        }
        int file = algebraic.charAt(0) - 'a';   // 0..7
        int rank = algebraic.charAt(1) - '1';   // 0..7
        int idx = (rank << 3) | file;
        validateSquare(idx);
        return idx;
    }

    /**
     * Converts a 0..63 index to algebraic notation (e.g., {@code 28 -> "e4"}).
     *
     * @param square index in [0..63]
     * @return algebraic coordinate string
     * @throws IllegalArgumentException if {@code square} is outside [0..63]
     */
    public static String toAlgebraic(int square) {
        validateSquare(square);
        char f = (char) ('a' + (square & 7));
        char r = (char) ('1' + (square >> 3));
        return "" + f + r;
    }

    /**
     * Validates that a square index lies within [0..63].
     *
     * @param pos index to validate
     * @throws IllegalArgumentException if {@code pos} is outside [0..63]
     */
    public static void validateSquare(int pos) {
        if (pos < 0 || pos >= 64) throw new IllegalArgumentException("square must be in [0,63], got " + pos);
    }
}
