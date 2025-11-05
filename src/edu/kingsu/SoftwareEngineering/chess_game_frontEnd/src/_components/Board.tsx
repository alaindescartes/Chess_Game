"use client";
import React from "react";

// --- Types & helpers --------------------------------------------------------
/**
 * Two-character piece codes used by the app:
 * - Leading char: 'w' or 'b' (white / black side)
 * - Trailing char: 'K', 'Q', 'R', 'B', 'N', 'P' (King…Pawn)
 *
 * These codes are shared by the backend API and the UI. The board omits
 * empty squares entirely from its {@link Position} map to keep payloads small.
 */

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

/**
 * Unicode chess glyphs for each piece code.
 * White: ♔♕♖♗♘♙  Black: ♚♛♜♝♞♟
 */
const GLYPHS: Record<Piece, string> = {
  wK: "♔",
  wQ: "♕",
  wR: "♖",
  wB: "♗",
  wN: "♘",
  wP: "♙",
  bK: "♚",
  bQ: "♛",
  bR: "♜",
  bB: "♝",
  bN: "♞",
  bP: "♟",
};
function glyphFor(piece: Piece): string {
  return GLYPHS[piece];
}

/**
 * Mapping from algebraic square (e.g., "e4") to a {@link Piece} code (e.g., "wN").
 * Only occupied squares are present; empty squares are omitted.
 */
export type Position = Record<string, Piece>;

// Accept either strict Position or a looser server-shaped map (Record<string, string>)
/**
 * Looser server-shaped map accepted by the component. Any non-piece values are
 * filtered out by {@link toPosition} before rendering.
 */
export type PositionLike = Position | Record<string, string> | undefined;

/** Type guard ensuring an arbitrary string is a valid {@link Piece} code. */
function isPiece(code: string): code is Piece {
  return (
    code === "wK" ||
    code === "wQ" ||
    code === "wR" ||
    code === "wB" ||
    code === "wN" ||
    code === "wP" ||
    code === "bK" ||
    code === "bQ" ||
    code === "bR" ||
    code === "bB" ||
    code === "bN" ||
    code === "bP"
  );
}

/**
 * Normalize any incoming payload into a strict {@link Position} by copying only
 * recognized {@link Piece} codes. Unknown keys/values are ignored.
 */
function toPosition(input: PositionLike): Position {
  if (!input) return initialPosition();
  const out: Position = {};
  for (const [sq, val] of Object.entries(input)) {
    if (typeof val === "string" && isPiece(val)) out[sq] = val;
  }
  return out;
}

/** File (a..h) and rank (1..8) labels used for square generation and edges. */
const FILES = ["a", "b", "c", "d", "e", "f", "g", "h"] as const;
const RANKS = [1, 2, 3, 4, 5, 6, 7, 8] as const;
/**
 * Build the canonical starting position (client-side fallback/testing only).
 * The server is still authoritative for real games.
 */
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
/**
 * Props for the {@link Board} component.
 *
 * @property size        Pixel number (e.g., 512) or CSS size string (e.g., "100%").
 * @property fullScreen  If true, the board keeps a perfect square that fits the viewport.
 * @property orientation Which side sits at the bottom ("white" | "black").
 * @property position    Position map (algebraic square -> piece code). Empties omitted.
 * @property selected    Currently selected source square, or null.
 * @property highlights  Squares to visually hint as candidate targets.
 * @property lastMove    Two-square tuple to outline the last move (from, to).
 * @property onSquareClick     Primary click handler for a square.
 * @property onSquareRightClick Context menu/right-click handler for a square.
 * @property renderPiece Custom renderer for a piece; overrides the default token.
 * @property onMoveIntent Fired when a source and a destination have been chosen.
 */
export interface BoardProps {
  /** Pixel number (e.g., 512) or CSS size string (e.g., "100%") */
  size?: number | string;
  /** Make the board occupy the full viewport (keeps a perfect square). */
  fullScreen?: boolean;
  /** Which side is at the bottom */
  orientation?: "white" | "black";
  /** Board position as a map of algebraic square -> piece code */
  position?: Position | Record<string, string>;
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
  /** Optional callback fired once both source and destination are chosen */
  onMoveIntent?: (from: string, to: string) => void;
}

/**
 * Chessboard UI component that renders an 8×8 grid, pieces, and interaction chrome
 * (selection rings, last-move outline, candidate dots, etc.).
 *
 * Interaction model:
 *  - First click selects a source square that contains a piece.
 *  - Second click selects a destination and triggers {@link BoardProps.onMoveIntent}.
 *  - Clicking a friendly-occupied destination re-selects that piece as the new source.
 *  - Clicking the same source again clears the selection.
 *
 * This component is presentational and does **not** perform full rule validation;
 * the backend remains authoritative.
 */
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
  onMoveIntent,
}: BoardProps) {
  const [fromSel, setFromSel] = React.useState<string | null>(selected ?? null);
  const [toSel, setToSel] = React.useState<string | null>(null);

  React.useEffect(() => {
    setFromSel(selected ?? null);
    setToSel(null);
  }, [selected]);
  const sel = fromSel;

  const pos: Position = React.useMemo(() => toPosition(position), [position]);

  const selectedPiece: Piece | null = React.useMemo(
    () => (sel ? (pos[sel] as Piece | undefined) ?? null : null),
    [sel, pos]
  );

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
      {/* Colored token renderer for clear white/black distinction */}
      <style>{`
        .hl-ring-blue { position:absolute; inset:4px; border-radius:6px; border:3px solid #2b6cb0; background: rgba(59,130,246,0.15); pointer-events:none; z-index:3; }
        .hl-ring-gold { position:absolute; inset:4px; border-radius:6px; border:3px solid rgba(255,215,0,0.95); background: transparent; pointer-events:none; z-index:2; }
        .hl-ring-last { position:absolute; inset:6px; border-radius:6px; border:3px solid rgba(0,0,0,0.35); background: transparent; pointer-events:none; z-index:1; }
        .hl-dot { position:absolute; top:50%; left:50%; width:28%; height:28%; transform:translate(-50%,-50%); border-radius:50%; background: rgba(16,185,129,0.85); box-shadow: 0 0 0 2px rgba(0,0,0,0.08) inset; pointer-events:none; z-index:4; }
        .hl-ring { position:absolute; inset:12%; border-radius:50%; border:6px solid rgba(220,38,38,0.9); background: transparent; pointer-events:none; z-index:4; }
      `}</style>
      <style>{`
        .chess-piece { display:inline-block; transition: transform 120ms ease, filter 120ms ease, opacity 120ms ease; will-change: transform; transform-origin: center bottom; animation: breathe 7s ease-in-out infinite; }
        .chess-piece:hover { transform: translateY(-3%) scale(1.06); filter: drop-shadow(0 6px 10px rgba(0,0,0,.35)); }
        @keyframes breathe { 0%, 100% { transform: translateY(0) } 50% { transform: translateY(-1.5%) } }
        @media (prefers-reduced-motion: reduce) { .chess-piece, .chess-piece:hover { animation: none !important; transition: none !important; transform: none !important; } }
      `}</style>
      <style>{`
        .piece-token {
          width: 78%;
          height: 78%;
          border-radius: 50%;
          display: flex;
          align-items: center;
          justify-content: center;
          font-weight: 800;
          font-size: calc(min(5.4vh, 5.4vw));
          line-height: 1;
          letter-spacing: .02em;
          box-shadow: 0 6px 12px rgba(0,0,0,.25), inset 0 0 0 2px rgba(0,0,0,.15);
          user-select: none;
        }
        .piece-glyph { line-height: 1; display: inline-block; transform: translateY(-2%); }
        .pt-white {
          background: radial-gradient(ellipse at 30% 30%, #ffffff, #e5e7eb 60%, #cbd5e1 100%);
          color: #111827;
        }
        .pt-black {
          background: radial-gradient(ellipse at 30% 30%, #111827, #0b1220 60%, #000000 100%);
          color: #f8fafc;
          box-shadow: 0 6px 12px rgba(0,0,0,.35), inset 0 0 0 2px rgba(255,255,255,.08);
        }
        @media (prefers-color-scheme: dark) {
          .pt-white { color: #0b1220; }
        }
      `}</style>

      <div style={boardStyle} role="grid" aria-label="Chessboard">
        {squares.map((sq) => {
          const file = sq[0];
          const rank = Number(sq[1]);
          const fileIdx = FILES.indexOf(file as (typeof FILES)[number]);
          const rankIdx = rank - 1;
          const isDark = (fileIdx + rankIdx) % 2 === 0; // a1 is dark
          const piece = (pos[sq] as Piece | undefined) ?? null;

          const isFrom = fromSel === sq;
          const isTo = toSel === sq;

          const isCandidate = !!fromSel && highlights.includes(sq);
          const isCapture =
            isCandidate &&
            !!piece &&
            !!selectedPiece &&
            piece[0] !== selectedPiece[0];
          const isLastMove = !!(
            lastMove &&
            (lastMove[0] === sq || lastMove[1] === sq)
          );

          const squareStyle: React.CSSProperties = {
            ...squareBase,
            background: isDark ? "#b58863" : "#f0d9b5",
            border: "1px solid rgba(0,0,0,0.08)",
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

          const handleClick = () => {
            if (!fromSel) {
              if (piece) {
                setFromSel(sq);
                setToSel(null);
              }
              onSquareClick?.(sq, piece);
              return;
            }

            if (fromSel && !toSel) {
              if (sq === fromSel) {
                setFromSel(null);
                setToSel(null);
              } else {
                if (piece && selectedPiece && piece[0] === selectedPiece[0]) {
                  setFromSel(sq);
                  setToSel(null);
                  onSquareClick?.(sq, piece);
                  return;
                }
                setToSel(sq);
                onMoveIntent?.(fromSel, sq);
              }
              onSquareClick?.(sq, piece);
              return;
            }

            if (sq === fromSel) {
              setFromSel(null);
              setToSel(null);
            } else if (piece) {
              setFromSel(sq);
              setToSel(null);
            } else {
              setToSel(sq);
              onMoveIntent?.(fromSel, sq);
            }
            onSquareClick?.(sq, piece);
          };
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
              {isLastMove && <span className="hl-ring-last" />}
              {isFrom && <span className="hl-ring-blue" />}
              {isTo && <span className="hl-ring-gold" />}
              {isCandidate && !isCapture && <span className="hl-dot" />}
              {isCandidate && isCapture && <span className="hl-ring" />}

              {piece && (
                <div
                  className="chess-piece"
                  aria-label={`${piece} on ${sq}`}
                  style={{ position: "relative", zIndex: 2 }}
                >
                  {renderPiece ? (
                    renderPiece(piece, sq)
                  ) : (
                    <div
                      className={`piece-token ${
                        piece[0] === "w" ? "pt-white" : "pt-black"
                      }`}
                    >
                      <span className="piece-glyph">{glyphFor(piece)}</span>
                    </div>
                  )}
                </div>
              )}

              {isBottomEdge && (
                <span style={{ ...labelChip, right: 4, bottom: 2 }}>
                  {file}
                </span>
              )}
              {isTopEdge && (
                <span style={{ ...labelChip, left: 4, top: 2 }}>{file}</span>
              )}

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
