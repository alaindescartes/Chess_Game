"use client";
import Board, { type Piece, type Position } from "@/_components/Board";
import FullscreenSkeleton from "@/_components/LoadingScreen";
import useCreateNewGame from "@/hooks/useCreateNewGame";
import React from "react";

export default function Home() {
  const [position, setPosition] = React.useState<Position>();
  const [selected, setSelected] = React.useState<string | null>(null);
  const [lastMove, setLastMove] = React.useState<[string, string] | null>(null);
  const [saving, setSaving] = React.useState(false);

  const { data, isLoading, error } = useCreateNewGame();

  React.useEffect(() => {
    if (data?.position) {
      setPosition(data.position as Position);
      setLastMove(
        data.lastFrom && data.lastTo
          ? ([data.lastFrom, data.lastTo] as [string, string])
          : null
      );
    }
  }, [data]);

  const showSkeleton = isLoading || !position;

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
      //const next = await saveMoveToServer(from, to, position);
      //setPosition(next);
      setLastMove([from, to]);
    } finally {
      setSelected(null);
      setSaving(false);
    }
  };

  return (
    <div>
      {showSkeleton && <FullscreenSkeleton />}
      <div style={{ position: "relative", display: "inline-block" }}>
        {!showSkeleton && (
          <Board
            position={position}
            selected={selected}
            lastMove={lastMove}
            onSquareClick={handleSquareClick}
          />
        )}
      </div>
      {saving && (
        <div className="saving-overlay" aria-live="polite" aria-busy="true">
          <div className="spinner" />
        </div>
      )}
      <style>{`
        @keyframes spin { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }
        .saving-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.08); display: flex; align-items: center; justify-content: center; z-index: 10000; backdrop-filter: blur(1px); }
        .spinner { width: 42px; height: 42px; border-radius: 50%; border: 4px solid rgba(255,255,255,0.35); border-top-color: #2563eb; animation: spin 0.9s linear infinite; }
      `}</style>

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
        {isLoading
          ? "Loading game…"
          : saving
          ? "Saving move…"
          : selected
          ? `Selected: ${selected} — click a target`
          : "Click a piece, then a target square"}
      </div>
    </div>
  );
}
