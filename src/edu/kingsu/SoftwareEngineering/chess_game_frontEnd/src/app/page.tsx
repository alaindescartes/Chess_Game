"use client";
import Board, { type Piece, type Position } from "@/_components/Board";
import FullscreenSkeleton from "@/_components/LoadingScreen";
import { useGameContext } from "@/context/GameContext";
import React from "react";
import useMakeMove from "@/hooks/useMakeMove";
import GameInfo from "@/_components/GameInfo";

export default function Home() {
  const [position, setPosition] = React.useState<Position>();
  const [selected, setSelected] = React.useState<string | null>(null);
  const [lastMove, setLastMove] = React.useState<[string, string] | null>(null);
  const [highlights, setHighlights] = React.useState<string[]>([]);
  const { game, isLoading, setGame } = useGameContext();
  const { sendMove, isLoading: moveLoading } = useMakeMove(game.gameId);

  React.useEffect(() => {
    if (game?.position) {
      setPosition(game.position);
      setLastMove(
        game.lastFrom && game.lastTo
          ? ([game.lastFrom, game.lastTo] as [string, string])
          : null
      );
    }
  }, [game]);

  const selectedPiece: Piece | null = React.useMemo(
    () => (selected && position ? (position[selected] as Piece) ?? null : null),
    [selected, position]
  );

  const showSkeleton = isLoading || !position;

  const handleSquareClick = async (square: string, piece: Piece | null) => {
    if (moveLoading) return;

    if (!selected) {
      if (!piece) return;
      setSelected(square);
      try {
        const targets = await fetchLegalmovesToHighlight(game.gameId, square);
        setHighlights(targets);
      } catch {
        setHighlights([]);
      }
      return;
    }

    if (square === selected) {
      setSelected(null);
      setHighlights([]);
      return;
    }

    if (piece && selectedPiece && piece[0] === selectedPiece[0]) {
      setSelected(square);
      try {
        const targets = await fetchLegalmovesToHighlight(game.gameId, square);
        setHighlights(targets);
      } catch {
        setHighlights([]);
      }
      return;
    }

    const from = selected as string;
    const to = square;
    try {
      const next = await sendMove({
        from,
        to,
        clientRev: game.rev,
        promotion: null,
      });
      setGame(next);
      setPosition(next.position);
      setLastMove(
        next.lastFrom && next.lastTo
          ? ([next.lastFrom, next.lastTo] as [string, string])
          : null
      );
    } finally {
      setSelected(null);
      setHighlights([]);
    }
  };

  async function fetchLegalmovesToHighlight(
    gameId: string,
    from: string
  ): Promise<string[]> {
    const res = await fetch(
      `http://localhost:8080/api/game/${gameId}/targets?from=${from}`,
      {
        method: "GET",
      }
    );
    if (!res.ok) throw new Error(`Targets fetch failed: ${res.status}`);
    return res.json();
  }

  return (
    <div>
      {showSkeleton && <FullscreenSkeleton />}
      <div className="game-shell">
        <div className="board-wrap">
          {!showSkeleton && (
            <Board
              position={position}
              selected={selected}
              highlights={highlights}
              lastMove={lastMove}
              onSquareClick={handleSquareClick}
            />
          )}
        </div>
        <aside className="info-panel">
          <GameInfo />
        </aside>
      </div>
      {moveLoading && (
        <div className="saving-overlay" aria-live="polite" aria-busy="true">
          <div className="spinner" />
        </div>
      )}
      <style>{`
        @keyframes spin { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }
        .saving-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.08); display: flex; align-items: center; justify-content: center; z-index: 10000; backdrop-filter: blur(1px); }
        .spinner { width: 42px; height: 42px; border-radius: 50%; border: 4px solid rgba(255,255,255,0.35); border-top-color: #2563eb; animation: spin 0.9s linear infinite; }

        .game-shell { display: flex; align-items: flex-start; gap: 16px; }
        .board-wrap { position: relative; display: inline-block; }
        .info-panel { min-width: 260px; max-width: 320px; padding: 8px 12px; border-radius: 10px; background: rgba(0,0,0,0.04); }
        @media (prefers-color-scheme: dark) { .info-panel { background: rgba(255,255,255,0.06); } }
        @media (max-width: 900px) { .game-shell { flex-direction: column; } .info-panel { width: 100%; max-width: none; }
        }
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
          : moveLoading
          ? "Saving move…"
          : selected
          ? highlights.length > 0
            ? `Selected: ${selected} — ${highlights.length} target${
                highlights.length === 1 ? "" : "s"
              }`
            : `Selected: ${selected}`
          : "Click a piece, then a target square"}
      </div>
    </div>
  );
}
