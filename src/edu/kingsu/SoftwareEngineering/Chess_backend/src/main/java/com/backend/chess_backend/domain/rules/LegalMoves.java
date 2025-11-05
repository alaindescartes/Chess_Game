package com.backend.chess_backend.domain.rules;

import com.backend.chess_backend.domain.Board;
import com.backend.chess_backend.domain.Piece;
import com.backend.chess_backend.domain.PieceColor;
import com.backend.chess_backend.domain.PieceType;
import com.backend.chess_backend.web.MoveRequest;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Domain service that calculates chess move legality.
 * <p>
 * Responsibilities:
 * <ul>
 *   <li>Provide <b>pseudo-legal</b> targets for a piece (movement pattern + bounds + occupancy rules).</li>
 *   <li>Provide a final <b>isLegal</b> check to be called by the application service before applying a move.</li>
 *   <li>(Optional) Generate all legal moves for a side and expose square-attack queries for king safety.</li>
 * </ul>
 * This class is intentionally stateless and has no persistence/web concerns; it operates on the domain model only.
 */
@Component
public class LegalMoves {

    /**
     * Compute pseudo-legal destination squares for the piece sitting on {@code from}.
     * <p>
     * Pseudo-legal = follows the piece's movement pattern, respects board boundaries, does not land on a friendly
     * piece, and for sliders (rook/bishop/queen) stops at the first blocker. <b>Does not</b> include king-safety
     * (pins / moving into check) nor special rules like castling and en-passant yet.
     *
     * @param board current board position
     * @param from  algebraic square ("a1".."h8") to generate moves from
     * @return list of destination squares (algebraic)
     */
    public List<String> pseudoLegalTargets(Board board, String from) {
        if (!isSquare(from)) return List.of();
        int fromIdx = Board.sq(from);
        Piece p = board.getAt(fromIdx);
        if (p == null) return List.of();

        return switch (p.getType()) {
            case PAWN -> pawnTargets(board, fromIdx, p.getColor());
            case KNIGHT -> knightTargets(board, fromIdx, p.getColor());
            case KING -> kingTargets(board, fromIdx, p.getColor());
            case ROOK -> slidingTargets(board, fromIdx, p.getColor(), ROOK_DIRS);
            case BISHOP -> slidingTargets(board, fromIdx, p.getColor(), BISHOP_DIRS);
            case QUEEN -> slidingTargets(board, fromIdx, p.getColor(), QUEEN_DIRS);
        };
    }

    /**
     * Final legality check that the application layer should call before applying a move.
     * <p>
     * This performs a cheap membership test against {@link #pseudoLegalTargets(Board, String)} and then (later)
     * verifies king safety (i.e., the move does not leave the mover's king in check). You can extend this to enforce
     * promotions, castling, and en-passant as you add those features.
     *
     * @param board current board
     * @param sideToMove side to move
     * @param req  requested move (from/to/promotion/clientRev)
     * @return true if legal under current rules; false otherwise
     */
    public boolean isLegal(Board board, PieceColor sideToMove, MoveRequest req) {
        if (req == null || !isSquare(req.from()) || !isSquare(req.to()) || req.from().equals(req.to())) {
            return false;
        }
        int from = Board.sq(req.from());
        int to = Board.sq(req.to());
        Piece mover = board.getAt(from);
        if (mover == null) return false;
        if (mover.getColor() != sideToMove) return false; // wrong side to move

        // Destination cannot be friendly occupied
        Piece dest = board.getAt(to);
        if (dest != null && dest.getColor() == mover.getColor()) return false;

        List<String> targets = pseudoLegalTargets(board, req.from());
        if (!targets.contains(req.to())) return false;

        // TODO: KING SAFETY
        return true; // basic rules only for now
    }

    private List<String> pawnTargets(Board board, int fromIdx, PieceColor side) {
        List<String> out = new ArrayList<>(4);
        int f = file(fromIdx), r = rank(fromIdx);
        int dir = (side == PieceColor.WHITE) ? +1 : -1;

        // one-step ahead (must be empty)
        int r1 = r + dir;
        int idx1 = idx(f, r1);
        if (inBounds(f, r1) && board.getAt(idx1) == null) {
            out.add(sq(f, r1));
            // two-step from starting rank (both squares empty)
            boolean atStart = (side == PieceColor.WHITE && r == 1) || (side == PieceColor.BLACK && r == 6);
            if (atStart) {
                int r2 = r + 2 * dir;
                int idx2 = idx(f, r2);
                if (inBounds(f, r2) && board.getAt(idx2) == null) {
                    out.add(sq(f, r2));
                }
            }
        }
        // diagonal captures if enemy
        int[] df = {-1, +1};
        for (int d : df) {
            int nf = f + d, nr = r + dir;
            if (!inBounds(nf, nr)) continue;
            int tidx = idx(nf, nr);
            Piece on = board.getAt(tidx);
            if (on != null && on.getColor() != side) out.add(sq(nf, nr));
        }
        // TODO:must implement enpassant
        return out;
    }

    private List<String> knightTargets(Board board, int fromIdx, PieceColor side) {
        List<String> out = new ArrayList<>(8);
        int f = file(fromIdx), r = rank(fromIdx);
        int[][] K = {{1,2},{2,1},{2,-1},{1,-2},{-1,-2},{-2,-1},{-2,1},{-1,2}};
        for (int[] d : K) {
            int nf = f + d[0], nr = r + d[1];
            if (!inBounds(nf, nr)) continue;
            Piece on = board.getAt(idx(nf, nr));
            if (on == null || on.getColor() != side) out.add(sq(nf, nr));
        }
        return out;
    }

    private List<String> kingTargets(Board board, int fromIdx, PieceColor side) {
        List<String> out = new ArrayList<>(8);
        int f = file(fromIdx), r = rank(fromIdx);
        for (int df = -1; df <= 1; df++) {
            for (int dr = -1; dr <= 1; dr++) {
                if (df == 0 && dr == 0) continue;
                int nf = f + df, nr = r + dr;
                if (!inBounds(nf, nr)) continue;
                Piece on = board.getAt(idx(nf, nr));
                if (on == null || on.getColor() != side) out.add(sq(nf, nr));
            }
        }
        // TODO: castling done, will be done later in next coming weeks (requires hasMoved flags + safety checks).
        return out;
    }

    private static final int[][] ROOK_DIRS   = {{1,0},{-1,0},{0,1},{0,-1}};
    private static final int[][] BISHOP_DIRS = {{1,1},{1,-1},{-1,1},{-1,-1}};
    private static final int[][] QUEEN_DIRS  = {{1,0},{-1,0},{0,1},{0,-1},{1,1},{1,-1},{-1,1},{-1,-1}};

    private List<String> slidingTargets(Board board, int fromIdx, PieceColor side, int[][] dirs) {
        List<String> out = new ArrayList<>(14);
        int f = file(fromIdx), r = rank(fromIdx);
        for (int[] d : dirs) {
            int nf = f + d[0], nr = r + d[1];
            while (inBounds(nf, nr)) {
                int tidx = idx(nf, nr);
                Piece on = board.getAt(tidx);
                if (on == null) {
                    out.add(sq(nf, nr));
                } else {
                    if (on.getColor() != side) out.add(sq(nf, nr)); // capture enemy and stop
                    break; // blocked
                }
                nf += d[0];
                nr += d[1];
            }
        }
        return out;
    }


    /** Returns true if mover's king would be in check after applying the move. */
    @SuppressWarnings("unused")
    private boolean leavesKingInCheck(Board board, int from, int to, PieceColor mover, PieceType promotion) {
        // TODO: clone board, apply move (and promotion if set), find mover's king, and call isSquareAttacked(...)
        throw new UnsupportedOperationException("King-safety not implemented yet");
    }

    /** Is {@code square} attacked by {@code bySide}? Useful for king safety & castling rules. */
    @SuppressWarnings("unused")
    private boolean isSquareAttacked(Board board, int squareIdx, PieceColor bySide) {
        // TODO: implement by scanning knights, pawns, king neighbors, and slider rays from bySide
        throw new UnsupportedOperationException("Attack detection not implemented yet");
    }


    // Coords & square helpers

    private static boolean isSquare(String s) {
        return s != null && s.length() == 2 && s.charAt(0) >= 'a' && s.charAt(0) <= 'h' && s.charAt(1) >= '1' && s.charAt(1) <= '8';
    }
    private static int file(int idx) { return idx & 7; }
    private static int rank(int idx) { return idx >>> 3; }
    private static int idx(int f, int r) { return (r << 3) | f; }
    private static String sq(int f, int r) { return "" + (char)('a' + f) + (char)('1' + r); }
    private static boolean inBounds(int f, int r) { return f >= 0 && f < 8 && r >= 0 && r < 8; }
}
