"use client";
import React from "react";

// --- Types & helpers --------------------------------------------------------
export type Piece =
  | "wK"
  | "wQ"
  | "wR"
  | "wB"
  | "wN"
  | "wP"
  | "bK"
  | "bQ"
  | "bR"
  | "bB"
  | "bN"
  | "bP";

export type Position = Record<string, Piece>;

const FILES = ["a", "b", "c", "d", "e", "f", "g", "h"] as const;
const RANKS = [1, 2, 3, 4, 5, 6, 7, 8] as const;

const PIECE_TO_CHAR: Record<Piece, string> = {
  wK: "\u2654",
  wQ: "\u2655",
  wR: "\u2656",
  wB: "\u2657",
  wN: "\u2658",
  wP: "\u2659",
  bK: "\u265A",
  bQ: "\u265B",
  bR: "\u265C",
  bB: "\u265D",
  bN: "\u265E",
  bP: "\u265F",
};

function initialPosition(): Position {
  const pos: Position = {};
  for (let i = 0; i < 8; i++) {
    pos[`${FILES[i]}2`] = "wP";
    pos[`${FILES[i]}7`] = "bP";
  }
  pos["a1"] = "wR";
  pos["h1"] = "wR";
  pos["a8"] = "bR";
  pos["h8"] = "bR";
  pos["b1"] = "wN";
  pos["g1"] = "wN";
  pos["b8"] = "bN";
  pos["g8"] = "bN";
  pos["c1"] = "wB";
  pos["f1"] = "wB";
  pos["c8"] = "bB";
  pos["f8"] = "bB";
  pos["d1"] = "wQ";
  pos["d8"] = "bQ";
  pos["e1"] = "wK";
  pos["e8"] = "bK";
  return pos;
}

// --- Component API ----------------------------------------------------------
export interface BoardProps {
  /** Pixel number (e.g., 512) or CSS size string (e.g., "100%") */
  size?: number | string;
  /** Make the board occupy the full viewport (keeps a perfect square). */
  fullScreen?: boolean;
  /** Which side is at the bottom */
  orientation?: "white" | "black";
  /** Board position as a map of algebraic square -> piece code */
  position?: Position;
  /** Currently selected square (e.g., "e4") */
  selected?: string | null;
  /** Squares to visually highlight (legal moves, hints, etc.) */
  highlights?: string[];
  /** Last move to outline, e.g., ["e2", "e4"] */
  lastMove?: [string, string] | null;
  /** Click/primary select */
  onSquareClick?: (square: string, piece: Piece | null) => void;
  /** Context menu / right-click action */
  onSquareRightClick?: (
    square: string,
    piece: Piece | null,
    ev: React.MouseEvent
  ) => void;
  /** Custom renderer for pieces (to swap emojis for images/SVG) */
  renderPiece?: (piece: Piece, square: string) => React.ReactNode;
}

function Board({
  size = 512,
  fullScreen = true,
  orientation = "white",
  position,
  selected = null,
  highlights = [],
  lastMove = null,
  onSquareClick,
  onSquareRightClick,
  renderPiece,
}: BoardProps) {
  const pos: Position = position ?? initialPosition();

  const squares = React.useMemo(() => {
    const files = orientation === "white" ? FILES : [...FILES].reverse();
    const ranks = orientation === "white" ? [...RANKS].reverse() : [...RANKS];
    const list: string[] = [];
    for (const r of ranks) {
      for (const f of files) list.push(`${f}${r}`);
    }
    return list;
  }, [orientation]);

  // Full-screen wrapper
  const computedSize = fullScreen
    ? "min(100dvw, 100dvh)"
    : typeof size === "number"
    ? `${size}px`
    : size;

  const wrapperStyle: React.CSSProperties | undefined = fullScreen
    ? {
        position: "fixed",
        inset: 0,
        display: "grid",
        placeItems: "center",
      }
    : undefined;

  const boardStyle: React.CSSProperties = {
    display: "grid",
    gridTemplateColumns: "repeat(8, 1fr)",
    gridTemplateRows: "repeat(8, 1fr)",
    width: computedSize,
    height: computedSize,
    userSelect: "none",
    border: "2px solid #444",
    borderRadius: 6,
    overflow: "hidden",
    background: "#0000",
  };

  const squareBase: React.CSSProperties = {
    position: "relative",
    display: "flex",
    alignItems: "center",
    justifyContent: "center",
    boxSizing: "border-box",
    lineHeight: 1,
  };

  const cornerLabel: React.CSSProperties = {
    position: "absolute",
    fontSize: 14,
    opacity: 0.95,
    pointerEvents: "none",
    fontWeight: 700,
    padding: "1px 4px",
    borderRadius: 4,
    letterSpacing: 0.3,
  };

  return (
    <div style={wrapperStyle}>
      <style>{`
        .chess-piece { display:inline-block; transition: transform 120ms ease, filter 120ms ease, opacity 120ms ease; will-change: transform; transform-origin: center bottom; animation: breathe 7s ease-in-out infinite; }
        .chess-piece:hover { transform: translateY(-3%) scale(1.06); filter: drop-shadow(0 6px 10px rgba(0,0,0,.35)); }
        @keyframes breathe { 0%, 100% { transform: translateY(0) } 50% { transform: translateY(-1.5%) } }
        @media (prefers-reduced-motion: reduce) { .chess-piece, .chess-piece:hover { animation: none !important; transition: none !important; transform: none !important; } }
      `}</style>

      <div style={boardStyle} role="grid" aria-label="Chessboard">
        {squares.map((sq) => {
          const file = sq[0];
          const rank = Number(sq[1]);
          const fileIdx = FILES.indexOf(file as (typeof FILES)[number]);
          const rankIdx = rank - 1;
          const isDark = (fileIdx + rankIdx) % 2 === 0; // a1 is dark
          const piece = (pos[sq] as Piece | undefined) ?? null;

          const isSelected = selected === sq;
          const isHighlighted = highlights.includes(sq);
          const isLastMove = !!(
            lastMove &&
            (lastMove[0] === sq || lastMove[1] === sq)
          );

          const squareStyle: React.CSSProperties = {
            ...squareBase,
            background: isDark ? "#b58863" : "#f0d9b5",
            outline: isSelected
              ? "3px solid #2b6cb0"
              : isHighlighted
              ? "3px solid rgba(255, 215, 0, 0.95)"
              : isLastMove
              ? "3px solid rgba(0,0,0,0.35)"
              : "1px solid rgba(0,0,0,0.08)",
            cursor: onSquareClick || piece ? "pointer" : "default",
            fontSize: "calc(min(6vh, 6vw))",
          };

          // Edge detection for labels
          const isBottomEdge =
            (orientation === "white" && rank === 1) ||
            (orientation === "black" && rank === 8);
          const isTopEdge =
            (orientation === "white" && rank === 8) ||
            (orientation === "black" && rank === 1);
          const isLeftEdge =
            (orientation === "white" && fileIdx === 0) ||
            (orientation === "black" && fileIdx === 7);
          const isRightEdge =
            (orientation === "white" && fileIdx === 7) ||
            (orientation === "black" && fileIdx === 0);

          const labelFg = isDark ? "#f7fafc" : "#1a202c";
          const labelBg = isDark
            ? "rgba(0,0,0,0.25)"
            : "rgba(255,255,255,0.65)";
          const labelChip: React.CSSProperties = {
            ...cornerLabel,
            color: labelFg,
            background: labelBg,
          };

          const handleClick = () => onSquareClick?.(sq, piece);
          const handleContext = (e: React.MouseEvent) => {
            e.preventDefault();
            onSquareRightClick?.(sq, piece, e);
          };

          return (
            <div
              key={sq}
              role="gridcell"
              aria-label={sq}
              style={squareStyle}
              onClick={handleClick}
              onContextMenu={handleContext}
            >
              {piece && (
                <div className="chess-piece" aria-label={`${piece} on ${sq}`}>
                  {renderPiece ? renderPiece(piece, sq) : PIECE_TO_CHAR[piece]}
                </div>
              )}

              {/* File labels: bottom-right & top-left to avoid overlap */}
              {isBottomEdge && (
                <span style={{ ...labelChip, right: 4, bottom: 2 }}>
                  {file}
                </span>
              )}
              {isTopEdge && (
                <span style={{ ...labelChip, left: 4, top: 2 }}>{file}</span>
              )}

              {/* Rank labels: bottom-left & top-right to avoid overlap */}
              {isLeftEdge && (
                <span style={{ ...labelChip, left: 4, bottom: 2 }}>{rank}</span>
              )}
              {isRightEdge && (
                <span style={{ ...labelChip, right: 4, top: 2 }}>{rank}</span>
              )}
            </div>
          );
        })}
      </div>
    </div>
  );
}

export default Board;
