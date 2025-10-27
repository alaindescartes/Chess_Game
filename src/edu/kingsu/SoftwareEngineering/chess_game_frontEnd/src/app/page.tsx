// "use client";
// import Board, { type Piece } from "@/_components/Board";

// export default function Home() {
//   const handleSquareClick = (square: string, piece: Piece | null) => {
//     console.log(
//       "[Board] Clicked square:",
//       square,
//       "piece:",
//       piece ?? "(empty)"
//     );
//   };

//   return (
//     <div>
//       <Board onSquareClick={handleSquareClick} />
//     </div>
//   );
// }

"use client";
import Board, { type Piece, type Position } from "@/_components/Board";
import React from "react";

// Minimal helper: starting position (copied locally so parent owns the state)
function initialPosition(): Position {
  const FILES = ["a", "b", "c", "d", "e", "f", "g", "h"] as const;
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

function saveMoveToServer(
  from: string,
  to: string,
  position: Position
): Promise<Position> {
  return new Promise((resolve) => {
    setTimeout(() => {
      const next: Position = { ...position };
      const moving = next[from];
      if (!moving) {
        resolve(position);
        return;
      }
      delete next[from];
      next[to] = moving;
      resolve(next);
    }, 650);
  });
}

export default function Home() {
  const [position, setPosition] = React.useState<Position>(initialPosition);
  const [selected, setSelected] = React.useState<string | null>(null);
  const [lastMove, setLastMove] = React.useState<[string, string] | null>(null);
  const [saving, setSaving] = React.useState(false);

  const handleSquareClick = async (square: string, piece: Piece | null) => {
    if (saving) return;

    if (!selected) {
      if (piece) setSelected(square);
      return;
    }

    if (square === selected) {
      setSelected(null);
      return;
    }

    setSaving(true);
    const from = selected;
    const to = square;
    try {
      const next = await saveMoveToServer(from, to, position);
      setPosition(next);
      setLastMove([from, to]);
    } finally {
      setSelected(null);
      setSaving(false);
    }
  };

  return (
    <div>
      <Board
        position={position}
        selected={selected}
        lastMove={lastMove}
        onSquareClick={handleSquareClick}
      />

      <div
        style={{
          position: "fixed",
          left: 12,
          bottom: 12,
          padding: "8px 10px",
          borderRadius: 8,
          background: "rgba(0,0,0,0.55)",
          color: "#fff",
          fontSize: 13,
          letterSpacing: 0.2,
        }}
        aria-live="polite"
      >
        {saving
          ? "Saving move…"
          : selected
          ? `Selected: ${selected} — click a target`
          : "Click a piece, then a target square"}
      </div>
    </div>
  );
}
