package com.backend.chess_backend.domain;

/**
 * Minimal in-memory model of a chess piece.
 * <p>
 * A piece has a {@link PieceColor}, a {@link PieceType}, its current square as a 0..63 index
 * (where a1 = 0, h1 = 7, a8 = 56, h8 = 63), and a flag indicating whether it has moved at least once.
 * This class holds no rule logic; move legality is handled elsewhere.
 * </p>
 *
 * <h2>Indexing scheme</h2>
 * <p>
 * Indices are encoded as <em>(rank << 3) | file</em>, where file ∈ [0..7] for a..h and rank ∈ [0..7] for 1..8.
 * Examples:
 * </p>
 * <pre>
 * a1 = (0 << 3) | 0 = 0
 * h1 = (0 << 3) | 7 = 7
 * a8 = (7 << 3) | 0 = 56
 * e4 = (3 << 3) | 4 = 28
 * </pre>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *   <li>Stores identity and square for a single piece.</li>
 *   <li>Does not validate legality of moves or turns.</li>
 *   <li>Range validation (0..63) is typically enforced by the board/engine; see method docs for specifics.</li>
 * </ul>
 *
 * @author Alain Uwishema
 * @since 0.1
 * @see PieceColor
 * @see PieceType
 */
public class Piece {
    private final PieceColor color;
    private final PieceType type;
    private int position;
    private boolean hasMoved;

    /**
     * Constructs a new piece instance.
     * <p>
     * This constructor does not perform range validation; callers should provide a square index in [0..63]
     * or delegate placement to the board setup utilities.
     * </p>
     *
     * <pre>
     * // Example: create a white pawn on e2 (index 12)
     * int e2 = (1 << 3) | 4; // rank=1, file=4
     * Piece p = new Piece(PieceColor.WHITE, PieceType.PAWN, e2);
     * </pre>
     *
     * @param color    the side that owns the piece (WHITE or BLACK)
     * @param type     the type of the piece (KING, QUEEN, ROOK, BISHOP, KNIGHT, PAWN)
     * @param position the board index 0..63 (a1 = 0 .. h8 = 63); no validation is done here
     * @since 0.1
     */
    public Piece(PieceColor color, PieceType type, int position) {
        this.color = color;
        this.type = type;
        this.position = position;
        this.hasMoved = false;
    }

    public PieceColor getColor() {
        return color;
    }

    public PieceType getType() {
        return type;
    }

    public int getPosition() {
        return position;
    }

    public boolean hasMoved() {
        return hasMoved;
    }

    public void setHasMoved(boolean hasMoved) {
        this.hasMoved = hasMoved;
    }
    /**
     * Sets the absolute board index for this piece <strong>without validation</strong> and
     * <strong>without</strong> toggling the {@code hasMoved} flag.
     * <p>
     * Intended for internal placement (e.g., initial setup). Game logic should validate destination squares
     * and set the moved flag as appropriate (e.g., via {@link #setHasMoved(boolean)}).
     * </p>
     *
     * <pre>
     * // Example: place piece on e4 (index 28) during setup
     * piece.moveTo((3 << 3) | 4);
     * piece.setHasMoved(false); // keep as not-moved after setup
     * </pre>
     *
     * @param position the target square index (expected 0..63); range is not checked here
     * @since 0.1
     */
    public void moveTo(int position) {
        this.position = position;
    }

    /**
     * Returns a concise representation for debugging (type, color, index, moved flag).
     *
     * @return human-readable summary of this piece's current state
     * @since 0.1
     */
    @Override
    public String toString() {
        return "Piece{" +
                "type=" + type +
                ", color=" + color +
                ", position=" + position +
                ", hasMoved=" + hasMoved +
                '}';
    }
}
