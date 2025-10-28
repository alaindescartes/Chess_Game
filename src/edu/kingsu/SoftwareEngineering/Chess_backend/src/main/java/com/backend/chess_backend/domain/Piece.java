package com.backend.chess_backend.entities;

public class Piece {
    private final PieceColor color;
    private final PieceType type;
    private int position;
    private boolean hasMoved;

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
    public void setPosition(int position) {
        if (position < 0 || position >= 63) {
            throw new IllegalArgumentException("Position out of range, it must be between 0 and 63");
        }
        this.position = position;
    }

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
